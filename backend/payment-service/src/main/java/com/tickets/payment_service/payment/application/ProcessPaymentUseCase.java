package com.tickets.payment_service.payment.application;

import com.tickets.payment_service.payment.application.dto.ProcessPaymentCommand;
import com.tickets.payment_service.payment.domain.Money;
import com.tickets.payment_service.payment.domain.OrderId;
import com.tickets.payment_service.payment.domain.Payment;
import com.tickets.payment_service.payment.domain.PaymentChargeResult;
import com.tickets.payment_service.payment.domain.PaymentStatus;
import com.tickets.payment_service.payment.domain.UserId;
import com.tickets.payment_service.payment.domain.port.PaymentEventPort;
import com.tickets.payment_service.payment.domain.port.PaymentGateway;
import com.tickets.payment_service.payment.domain.port.PaymentRepository;
import com.tickets.payment_service.shared.annotation.UseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caso de uso: procesar un pago recibido desde ticket-service.
 *
 * Orquesta la lógica de negocio sin conocer detalles de Stripe, RabbitMQ ni JPA.
 * La estrategia de cobro, persistencia y publicación de eventos se delega
 * a los puertos secundarios definidos en el dominio.
 *
 * Nota sobre transacciones: no se usa @Transactional a nivel del use case para evitar
 * mantener una conexión de BD abierta durante la llamada de red al gateway de pagos.
 * Cada save() es autocommitted por Spring Data de forma independiente.
 */
@UseCase
public class ProcessPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPaymentUseCase.class);

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentEventPort paymentEventPort;

    public ProcessPaymentUseCase(PaymentRepository paymentRepository,
                                  PaymentGateway paymentGateway,
                                  PaymentEventPort paymentEventPort) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.paymentEventPort = paymentEventPort;
    }

    public void execute(ProcessPaymentCommand command) {
        OrderId orderId = OrderId.of(command.orderId());

        // Idempotencia: si ya existe un pago para esta orden, republicar el evento
        // en caso de que el mensaje anterior haya fallado después de guardar en DB
        var existing = paymentRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            Payment existingPayment = existing.get();
            if (existingPayment.getStatus() == PaymentStatus.APPROVED) {
                log.warn("[UC] ProcessPayment — pago ya APPROVED, republicando evento: orderId={}", command.orderId());
                paymentEventPort.publishPaymentCompleted(existingPayment);
            } else if (existingPayment.getStatus() == PaymentStatus.REJECTED) {
                log.warn("[UC] ProcessPayment — pago ya REJECTED, republicando evento: orderId={}", command.orderId());
                paymentEventPort.publishPaymentFailed(orderId, UserId.of(command.userId()), "Pago previamente rechazado");
            } else {
                log.warn("[UC] ProcessPayment — pago en estado {}, ignorando: orderId={}", existingPayment.getStatus(), command.orderId());
            }
            return;
        }

        log.info("[UC] ProcessPayment — iniciando: orderId={}, userId={}, amount={} {}",
                command.orderId(), command.userId(), command.amount(), command.currency());

        Money money = Money.of(command.amount(), command.currency());
        UserId userId = UserId.of(command.userId());

        // Crear el aggregate en estado PENDING y persistir antes del cobro
        Payment payment = Payment.create(orderId, userId, money, command.paymentIntentId());
        payment = paymentRepository.save(payment);

        // Delegar el cobro al gateway — nunca lanza excepción; el resultado lo informa
        PaymentChargeResult result = paymentGateway.charge(money, command.paymentIntentId(), orderId);

        // El aggregate aplica la lógica de transición de estado
        if (result.succeeded()) {
            payment.confirm(result.transactionId());
            paymentRepository.save(payment);
            paymentEventPort.publishPaymentCompleted(payment);
            log.info("[UC] ProcessPayment — aprobado: orderId={}, transactionId={}",
                    command.orderId(), result.transactionId());
        } else {
            payment.reject(result.failureReason());
            paymentRepository.save(payment);
            paymentEventPort.publishPaymentFailed(orderId, userId, result.failureReason());
            log.warn("[UC] ProcessPayment — rechazado: orderId={}, reason={}",
                    command.orderId(), result.failureReason());
        }
    }
}

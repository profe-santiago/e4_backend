package com.tickets.payment_service.payment.application;

import com.tickets.payment_service.exception.PaymentNotFoundException;
import com.tickets.payment_service.payment.domain.OrderId;
import com.tickets.payment_service.payment.domain.Payment;
import com.tickets.payment_service.payment.domain.port.PaymentRepository;
import com.tickets.payment_service.shared.annotation.UseCase;

@UseCase
public class FindPaymentByOrderUseCase {

    private final PaymentRepository paymentRepository;

    public FindPaymentByOrderUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment execute(OrderId orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for orderId: " + orderId.value()));
    }
}

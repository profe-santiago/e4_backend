package com.tickets.payment_service.payment.application;

import com.tickets.payment_service.exception.PaymentNotFoundException;
import com.tickets.payment_service.payment.domain.Payment;
import com.tickets.payment_service.payment.domain.PaymentId;
import com.tickets.payment_service.payment.domain.port.PaymentRepository;
import com.tickets.payment_service.shared.annotation.UseCase;

@UseCase
public class FindPaymentByIdUseCase {

    private final PaymentRepository paymentRepository;

    public FindPaymentByIdUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment execute(PaymentId paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for id: " + paymentId.value()));
    }
}

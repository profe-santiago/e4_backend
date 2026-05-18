package com.tickets.payment_service.payment.application;

import com.tickets.payment_service.payment.domain.CreateIntentResult;
import com.tickets.payment_service.payment.domain.Money;
import com.tickets.payment_service.payment.domain.port.PaymentGateway;
import com.tickets.payment_service.shared.annotation.UseCase;

@UseCase
public class CreatePaymentIntentUseCase {

    private final PaymentGateway paymentGateway;

    public CreatePaymentIntentUseCase(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public CreateIntentResult execute(Money amount) {
        return paymentGateway.createIntent(amount);
    }
}

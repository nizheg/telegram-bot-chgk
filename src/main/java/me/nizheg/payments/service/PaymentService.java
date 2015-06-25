package me.nizheg.payments.service;

import me.nizheg.payments.dto.PaymentStatus;
import me.nizheg.payments.dto.PaymentTransaction;

/**
 * @author Nikolay Zhegalin
 */
public interface PaymentService {
    PaymentTransaction initPayment(PaymentProvider paymentProvider);

    void updateStatus(long transactionId, PaymentStatus status, String details);

    PaymentTransaction getPaymentTransaction(long transactionId);
}

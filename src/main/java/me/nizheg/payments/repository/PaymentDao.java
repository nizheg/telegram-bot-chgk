package me.nizheg.payments.repository;

import me.nizheg.payments.dto.PaymentStatus;
import me.nizheg.payments.dto.PaymentTransaction;

/**
 * @author Nikolay Zhegalin
 */
public interface PaymentDao {
    long create(PaymentTransaction paymentTransaction);

    void updateStatus(long transactionId, PaymentStatus status, String details);

    PaymentTransaction getById(long transactionId);
}

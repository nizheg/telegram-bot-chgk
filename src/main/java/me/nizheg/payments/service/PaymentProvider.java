package me.nizheg.payments.service;

import me.nizheg.payments.dto.PaymentParameters;

/**
 * @author Nikolay Zhegalin
 */
public interface PaymentProvider<T extends PaymentParameters> {
    void setPaymentParameters(T paymentParameters);

    T getPaymentParameters();

    String getPaymentUrl() throws PaymentException;
}

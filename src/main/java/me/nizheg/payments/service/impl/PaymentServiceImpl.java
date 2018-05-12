package me.nizheg.payments.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.nizheg.payments.dto.PaymentParameters;
import me.nizheg.payments.dto.PaymentStatus;
import me.nizheg.payments.dto.PaymentTransaction;
import me.nizheg.payments.repository.PaymentDao;
import me.nizheg.payments.service.PaymentProvider;
import me.nizheg.payments.service.PaymentService;

/**
 * @author Nikolay Zhegalin
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentDao paymentDao;

    public PaymentServiceImpl(PaymentDao paymentDao) {this.paymentDao = paymentDao;}

    @Override
    @Transactional
    public PaymentTransaction initPayment(PaymentProvider paymentProvider) {
        PaymentParameters paymentParameters = paymentProvider.getPaymentParameters();
        PaymentTransaction paymentTransaction = new PaymentTransaction(paymentParameters.getTelegramUserId(), paymentParameters.getSum());
        long id = paymentDao.create(paymentTransaction);
        paymentParameters.setTransactionId(String.valueOf(id));
        return paymentDao.getById(id);
    }

    @Override
    public void updateStatus(long transactionId, PaymentStatus status, String details) {
        paymentDao.updateStatus(transactionId, status, details);
    }

    @Override
    public PaymentTransaction getPaymentTransaction(long transactionId) {
        return paymentDao.getById(transactionId);
    }

}

package me.nizheg.payments.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Nikolay Zhegalin
 */
public class PaymentParameters implements Serializable {
    private long telegramUserId;
    private BigDecimal sum;
    private String transactionId;

    public PaymentParameters(Long telegramUserId, BigDecimal sum) {
        this.telegramUserId = telegramUserId;
        this.sum = sum;
    }

    public long getTelegramUserId() {
        return telegramUserId;
    }

    public void setTelegramUserId(long telegramUserId) {
        this.telegramUserId = telegramUserId;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}

package me.nizheg.payments.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Nikolay Zhegalin
 */
public class PaymentTransaction implements Serializable {

    private static final long serialVersionUID = 6283562065106824949L;
    private final Long id;
    private final Long telegramUserId;
    private final Date creationTime;
    private final BigDecimal sum;
    private PaymentStatus status;
    private String result;

    public PaymentTransaction(Long telegramUserId, BigDecimal sum) {
        this(null, telegramUserId, sum);
    }

    public PaymentTransaction(Long id, Long telegramUserId, BigDecimal sum) {
        this(id, telegramUserId, sum, new Date());
    }

    public PaymentTransaction(Long id, Long telegramUserId, BigDecimal sum, Date creationTime) {
        this.id = id;
        this.telegramUserId = telegramUserId;
        this.sum = sum;
        this.creationTime = creationTime;
        this.status = PaymentStatus.CREATED;
    }

    public Long getId() {
        return id;
    }

    public Long getTelegramUserId() {
        return telegramUserId;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}

package me.nizheg.payments.yandex.model;

import java.math.BigDecimal;

/**
 * @author Nikolay Zhegalin
 */
public class PaymentCallback {
    private String notificationType;
    private String operationId;
    private BigDecimal amount;
    private BigDecimal withdrawAmount;
    private String currency;
    private String datetime;
    private String sender;
    private boolean codepro;
    private String label;
    private String sha1Hash;
    private boolean isUnaccepted;

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getWithdrawAmount() {
        return withdrawAmount;
    }

    public void setWithdrawAmount(BigDecimal withdrawAmount) {
        this.withdrawAmount = withdrawAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public boolean isCodepro() {
        return codepro;
    }

    public void setCodepro(boolean codepro) {
        this.codepro = codepro;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSha1Hash() {
        return sha1Hash;
    }

    public void setSha1Hash(String sha1Hash) {
        this.sha1Hash = sha1Hash;
    }

    public boolean isUnaccepted() {
        return isUnaccepted;
    }

    public void setUnaccepted(boolean isUnaccepted) {
        this.isUnaccepted = isUnaccepted;
    }

    @Override
    public String toString() {
        return "PaymentCallback{" + "notificationType='" + notificationType + '\'' + ", operationId='" + operationId + '\'' + ", amount=" + amount
                + ", withdrawAmount=" + withdrawAmount + ", currency='" + currency + '\'' + ", datetime=" + datetime + ", sender='" + sender + '\''
                + ", codepro=" + codepro + ", label='" + label + '\'' + ", sha1Hash='" + sha1Hash + '\'' + ", isUnaccepted=" + isUnaccepted + '}';
    }
}

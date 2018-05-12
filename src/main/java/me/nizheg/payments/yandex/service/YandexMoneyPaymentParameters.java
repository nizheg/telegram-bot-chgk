package me.nizheg.payments.yandex.service;

import java.math.BigDecimal;

import me.nizheg.payments.dto.PaymentParameters;
import me.nizheg.payments.yandex.model.YandexMoneyPayForm;
import me.nizheg.payments.yandex.model.YandexMoneyPaymentType;

/**
 * @author Nikolay Zhegalin
 */
public class YandexMoneyPaymentParameters extends PaymentParameters {
    private String receiver;
    private YandexMoneyPayForm payForm;
    private String targets;
    private YandexMoneyPaymentType[] paymentTypes;

    public YandexMoneyPaymentParameters(long telegramUserId, BigDecimal sum) {
        super(telegramUserId, sum);
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public YandexMoneyPayForm getPayForm() {
        return payForm;
    }

    public void setPayForm(YandexMoneyPayForm payForm) {
        this.payForm = payForm;
    }

    public String getTargets() {
        return targets;
    }

    public void setTargets(String targets) {
        this.targets = targets;
    }

    public YandexMoneyPaymentType[] getPaymentTypes() {
        return paymentTypes;
    }

    public void setPaymentTypes(YandexMoneyPaymentType[] paymentTypes) {
        this.paymentTypes = paymentTypes;
    }
}

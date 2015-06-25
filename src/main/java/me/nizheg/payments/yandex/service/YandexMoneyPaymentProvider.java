package me.nizheg.payments.yandex.service;

import java.io.IOException;
import java.net.URL;

import me.nizheg.payments.service.PaymentException;
import me.nizheg.payments.service.PaymentProvider;

import me.nizheg.payments.yandex.model.YandexMoneyPaymentType;
import me.nizheg.payments.yandex.service.YandexMoneyPaymentParameters;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

/**
 * @author Nikolay Zhegalin
 */
public class YandexMoneyPaymentProvider implements PaymentProvider<YandexMoneyPaymentParameters> {

    private YandexMoneyPaymentParameters parameters;

    @Override
    public YandexMoneyPaymentParameters getPaymentParameters() {
        return parameters;
    }

    @Override
    public void setPaymentParameters(YandexMoneyPaymentParameters paymentParameters) {
        this.parameters = paymentParameters;
    }

    @Override
    public String getPaymentUrl() throws PaymentException {
        if (parameters == null) {
            throw new IllegalStateException("Parameters are not defined yet");
        }
        Connection connection = Jsoup.connect("https://money.yandex.ru/quickpay/cps-preparation.xml");
        connection.data("receiver", parameters.getReceiver());
        connection.data("sum", String.valueOf(parameters.getSum()));
        connection.data("quickpay-form", parameters.getPayForm().name().toLowerCase());
        connection.data("targets", parameters.getTargets());
        for (YandexMoneyPaymentType yandexMoneyPaymentType : parameters.getPaymentTypes()) {
            connection.data("paymentType", yandexMoneyPaymentType.name());
        }
        connection.data("label", parameters.getTransactionId());
        connection.method(Connection.Method.GET);
        Connection.Response response;
        try {
            response = connection.execute();
        } catch (IOException e) {
            throw new PaymentException(e);
        }
        URL url = response.url();
        return url.toString();
    }
}

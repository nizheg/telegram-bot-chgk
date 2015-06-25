package me.nizheg.payments.yandex.service;

import javax.annotation.PostConstruct;

import me.nizheg.payments.dto.PaymentStatus;
import me.nizheg.payments.service.PaymentService;
import me.nizheg.payments.yandex.model.PaymentCallback;
import me.nizheg.telegram.bot.service.PropertyService;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Nikolay Zhegalin
 */
@Service
public class YandexPaymentCallbackService {
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private PaymentService paymentService;
    private String secret;
    private Log logger = LogFactory.getLog(getClass());

    @PostConstruct
    public void init() {
        secret = propertyService.getValue("money.yandex.secret");
    }

    public void processPaymentCallback(PaymentCallback paymentCallback) {
        logger.info("CALLBACK received " + paymentCallback.toString());
        Long transactionId = null;
        try {
            transactionId = Long.valueOf(paymentCallback.getLabel());
        } catch (NumberFormatException ex) {
            logger.error("Label is incorrect", ex);
        }
        if (paymentCallback.isCodepro()) {
            logger.error("CALLBACK codepro " + transactionId);
            if (transactionId != null) {
                paymentService.updateStatus(transactionId, PaymentStatus.FAILED, "Payment is code protected");
            }
            return;
        }
        if (paymentCallback.isUnaccepted()) {
            logger.error("CALLBACK unaccepted " + transactionId);
            if (transactionId != null) {
                paymentService.updateStatus(transactionId, PaymentStatus.FAILED, "Payment is unaccepted");
            }
            return;
        }
        String paymentCallbackSha1Hex = calculateSha1Hex(paymentCallback);
        if (!paymentCallbackSha1Hex.equals(paymentCallback.getSha1Hash())) {
            logger.error("CALLBACK sha1 incorrect. actual:" + paymentCallbackSha1Hex + " required:" + paymentCallback.getSha1Hash());
            if (transactionId != null) {
                paymentService.updateStatus(transactionId, PaymentStatus.FAILED,
                        "SHA не верен: " + paymentCallbackSha1Hex + " Ожидается " + paymentCallback.getSha1Hash());
            }
            return;
        }
        if (transactionId != null) {
            paymentService.updateStatus(transactionId, PaymentStatus.SUCCESS, paymentCallback.toString());
        }
    }

    private String calculateSha1Hex(PaymentCallback paymentCallback) {
        String data =
                paymentCallback.getNotificationType() + "&" + paymentCallback.getOperationId() + "&" + paymentCallback.getAmount() + "&"
                        + paymentCallback.getCurrency() + "&" + paymentCallback.getDatetime() + "&" + paymentCallback.getSender() + "&"
                        + paymentCallback.isCodepro() + "&" + secret + "&" + paymentCallback.getLabel();
        return DigestUtils.sha1Hex(data);
    }
}

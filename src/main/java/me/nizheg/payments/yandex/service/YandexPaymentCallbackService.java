package me.nizheg.payments.yandex.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

import me.nizheg.payments.dto.PaymentStatus;
import me.nizheg.payments.service.PaymentService;
import me.nizheg.payments.yandex.model.PaymentCallback;
import me.nizheg.telegram.bot.service.PropertyService;

/**
 * @author Nikolay Zhegalin
 */
@Service
public class YandexPaymentCallbackService {

    private final PropertyService propertyService;
    private final PaymentService paymentService;
    private String secret;
    private final Log logger = LogFactory.getLog(getClass());

    public YandexPaymentCallbackService(
            @Nonnull PropertyService propertyService,
            @Nonnull PaymentService paymentService) {
        Validate.notNull(propertyService, "propertyService should be defined");
        Validate.notNull(paymentService, "paymentService should be defined");
        this.propertyService = propertyService;
        this.paymentService = paymentService;
    }

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
            logger.error("CALLBACK sha1 incorrect. actual:" + paymentCallbackSha1Hex + " required:"
                    + paymentCallback.getSha1Hash());
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
                paymentCallback.getNotificationType() + "&" + paymentCallback.getOperationId() + "&"
                        + paymentCallback.getAmount() + "&"
                        + paymentCallback.getCurrency() + "&" + paymentCallback.getDatetime() + "&"
                        + paymentCallback.getSender() + "&"
                        + paymentCallback.isCodepro() + "&" + secret + "&" + paymentCallback.getLabel();
        return DigestUtils.sha1Hex(data);
    }
}

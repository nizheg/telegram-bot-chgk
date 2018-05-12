package me.nizheg.payments.yandex.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import me.nizheg.payments.yandex.model.PaymentCallback;
import me.nizheg.payments.yandex.service.YandexPaymentCallbackService;

/**
 * @author Nikolay Zhegalin
 */
@Controller
public class PaymentCallbackController {

    private final YandexPaymentCallbackService yandexPaymentCallbackService;

    public PaymentCallbackController(@Nonnull YandexPaymentCallbackService yandexPaymentCallbackService) {
        Validate.notNull(yandexPaymentCallbackService, "yandexPaymentCallbackService should be defined");
        this.yandexPaymentCallbackService = yandexPaymentCallbackService;
    }

    @RequestMapping(value = "/api/payment/yandex/result", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void paymentResult(
            @RequestParam(value = "notification_type", required = false) String notificationType,
            @RequestParam(value = "operation_id", required = false) String operationId,
            @RequestParam(value = "amount", required = false) BigDecimal amount,
            @RequestParam(value = "withdraw_amount", required = false) BigDecimal withdrawAmount,
            @RequestParam(value = "currency", required = false) String currency,
            @RequestParam(value = "datetime", required = false) String datetime,
            @RequestParam(value = "sender", required = false) String sender,
            @RequestParam(value = "codepro", required = false) boolean codepro,
            @RequestParam(value = "label", required = false) String label,
            @RequestParam(value = "sha1_hash", required = false) String sha1Hash,
            @RequestParam(value = "unaccepted", required = false) boolean isUnaccepted
    ) {
        PaymentCallback paymentCallback = new PaymentCallback();
        paymentCallback.setNotificationType(notificationType);
        paymentCallback.setOperationId(operationId);
        paymentCallback.setAmount(amount);
        paymentCallback.setWithdrawAmount(withdrawAmount);
        paymentCallback.setCurrency(currency);
        paymentCallback.setDatetime(datetime);
        paymentCallback.setSender(StringUtils.defaultString(sender));
        paymentCallback.setCodepro(codepro);
        paymentCallback.setLabel(StringUtils.defaultString(label));
        paymentCallback.setSha1Hash(sha1Hash);
        paymentCallback.setUnaccepted(isUnaccepted);
        yandexPaymentCallbackService.processPaymentCallback(paymentCallback);
    }

}

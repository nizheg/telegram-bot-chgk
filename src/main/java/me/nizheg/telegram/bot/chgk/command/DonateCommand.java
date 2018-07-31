package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import me.nizheg.payments.dto.PaymentStatus;
import me.nizheg.payments.dto.PaymentTransaction;
import me.nizheg.payments.service.PaymentException;
import me.nizheg.payments.service.PaymentService;
import me.nizheg.payments.yandex.YandexPaymentProperties;
import me.nizheg.payments.yandex.model.YandexMoneyPayForm;
import me.nizheg.payments.yandex.model.YandexMoneyPaymentType;
import me.nizheg.payments.yandex.service.YandexMoneyPaymentParameters;
import me.nizheg.payments.yandex.service.YandexMoneyPaymentProvider;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.EditedMessage;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.bot.service.PropertyService;
import me.nizheg.telegram.util.Emoji;
import me.nizheg.telegram.util.TelegramApiUtil;

/**
 * @author Nikolay Zhegalin
 */
public class DonateCommand extends ChatCommand {

    private final PaymentService paymentService;
    private final PropertyService propertyService;
    private final BigDecimal minSum = new BigDecimal(10);

    public DonateCommand(
            @Nonnull TelegramApiClient telegramApiClient,
            @Nonnull PaymentService paymentService,
            @Nonnull PropertyService propertyService) {
        super(telegramApiClient);
        Validate.notNull(paymentService, "paymentService should be defined");
        Validate.notNull(propertyService, "propertyService should be defined");
        this.paymentService = paymentService;
        this.propertyService = propertyService;
    }

    public DonateCommand(
            Supplier<TelegramApiClient> telegramApiClientSupplier,
            @Nonnull PaymentService paymentService,
            @Nonnull PropertyService propertyService) {
        super(telegramApiClientSupplier);
        Validate.notNull(paymentService, "paymentService should be defined");
        Validate.notNull(propertyService, "propertyService should be defined");
        this.paymentService = paymentService;
        this.propertyService = propertyService;
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        if (!ctx.isPrivateChat()) {
            getTelegramApiClient().sendMessage(
                    new Message("Воспользуйтесь, пожалуйста, данной командой в личке с ботом", ctx.getChatId()));
            return;
        }
        String regexp = "\\s*([0-9]+(?:\\.[0-9]+)?)(?: (AC|PC|MC))?\\s*";
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(ctx.getText());
        String sumParam = null;
        String paymentTypeParam = null;
        if (matcher.matches()) {
            sumParam = matcher.group(1);
            paymentTypeParam = matcher.group(2);
        }
        Message incorrectSumMessage =
                new Message(
                        "Укажите сумму в рублях не менее 10. Например, <code>/donate 100.00</code> или <code>/donate 100</code>",
                        ctx.getChatId(),
                        ParseMode.HTML);
        if (sumParam == null) {
            getTelegramApiClient().sendMessage(incorrectSumMessage);
            return;
        }
        BigDecimal sum = new BigDecimal(sumParam);
        if (sum.compareTo(minSum) < 0) {
            getTelegramApiClient().sendMessage(incorrectSumMessage);
            return;
        }

        if (paymentTypeParam == null) {
            Message message = new Message("<b>Сумма</b>: " + sum + " руб.", ctx.getChatId(), ParseMode.HTML);
            message.setReplyMarkup(TelegramApiUtil.createInlineButtonMarkup(Emoji.PURSE + " Я.Деньги",
                    "donate " + sum + " " + YandexMoneyPaymentType.PC.name(), Emoji.CREDIT_CARD + " Карточка",
                    "donate " + sum + " "
                            + YandexMoneyPaymentType.AC.name()));
            getTelegramApiClient().sendMessage(message);
            return;
        }

        YandexMoneyPaymentType yandexMoneyPaymentType = YandexMoneyPaymentType.valueOf(paymentTypeParam);
        YandexMoneyPaymentProvider paymentProvider = new YandexMoneyPaymentProvider();
        YandexMoneyPaymentParameters parameters = new YandexMoneyPaymentParameters(ctx.getFrom().getId(), sum);
        String receiver = propertyService.getValue(YandexPaymentProperties.RECEIVER);
        parameters.setReceiver(receiver);
        String target = propertyService.getValue(YandexPaymentProperties.TARGET);
        parameters.setTargets(target);
        parameters.setPaymentTypes(new YandexMoneyPaymentType[] {yandexMoneyPaymentType});
        parameters.setPayForm(YandexMoneyPayForm.DONATE);
        paymentProvider.setPaymentParameters(parameters);
        PaymentTransaction paymentTransaction = paymentService.initPayment(paymentProvider);
        String paymentDescription = "";
        switch (yandexMoneyPaymentType) {
            case PC:
                paymentDescription = Emoji.PURSE + " Яндекс Кошелек";
                break;
            case AC:
                paymentDescription = Emoji.CREDIT_CARD + " Банковская карта";
                break;
        }
        try {
            String paymentUrl = paymentProvider.getPaymentUrl();
            Message message = new Message("<b>Сумма:</b> " + sum + " руб.\n<b>Способ:</b> " + paymentDescription,
                    ctx.getChatId(), ParseMode.HTML);
            message.setReplyMarkup(TelegramApiUtil.createInlineUrlMarkup("Перевести", paymentUrl));
            Long messageId = ctx.getReplyToBotMessage().getMessageId();
            if (messageId != null) {
                getTelegramApiClient().editMessageText(new EditedMessage(message, messageId));
            } else {
                getTelegramApiClient().sendMessage(message);
            }
        } catch (PaymentException | RuntimeException e) {
            paymentService.updateStatus(paymentTransaction.getId(), PaymentStatus.FAILED,
                    "Не удалось инициализировать оплату: " + e.getMessage());
            throw new CommandException(
                    "Не удалось инициализировать оплату. Попробуйте позднее или воспользуйтесь ссылкой "
                            + getPaymentDirectLink(), e);
        }

    }

    @Override
    public String getCommandName() {
        return "donate";
    }

    @Override
    public String getDescription() {
        return "/donate <sum> - внести пожертвование на оплату сервера (<sum> рублей)";
    }

    private String getPaymentDirectLink() {
        String receiver = propertyService.getValue(YandexPaymentProperties.RECEIVER);
        return "https://money.yandex.ru/to/" + receiver;
    }
}

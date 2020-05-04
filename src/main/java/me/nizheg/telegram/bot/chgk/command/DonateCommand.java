package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

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
import me.nizheg.telegram.bot.api.model.InlineKeyboardMarkup;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.EditedMessage;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.bot.service.PropertyService;
import me.nizheg.telegram.util.Emoji;

import static me.nizheg.telegram.bot.api.model.InlineKeyboardButton.callbackDataButton;
import static me.nizheg.telegram.bot.api.model.InlineKeyboardButton.urlButton;

/**
 * @author Nikolay Zhegalin
 */
@Component
public class DonateCommand extends ChatCommand {

    private final PaymentService paymentService;
    private final PropertyService propertyService;
    private final BigDecimal minSum = new BigDecimal(10);

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
    public int getPriority() {
        return 170;
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        if (!ctx.isPrivateChat()) {
            getTelegramApiClient().sendMessage(Message.safeMessageBuilder()
                    .text("Воспользуйтесь, пожалуйста, данной командой в личке с ботом")
                    .chatId(new ChatId(ctx.getChatId()))
                    .build());
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
        String receiver = propertyService.getValue(YandexPaymentProperties.RECEIVER);
        parameters.setReceiver(receiver);
        Message incorrectSumMessage = Message.safeMessageBuilder()
                .text("Внести пожертвование в поддержку проекта можно <a href=\"https://money.yandex.ru/to/" + receiver + "\">здесь</a>")
                .chatId(new ChatId(ctx.getChatId()))
                .parseMode(ParseMode.HTML)
                .build();
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
            Message message = Message.safeMessageBuilder()
                    .text("<b>Сумма</b>: " + sum + " руб.")
                    .chatId(new ChatId(ctx.getChatId()))
                    .parseMode(ParseMode.HTML)
                    .replyMarkup(InlineKeyboardMarkup.oneRow(
                            callbackDataButton(Emoji.PURSE + " Я.Деньги",
                                    "donate " + sum + " " + YandexMoneyPaymentType.PC.name()),
                            callbackDataButton(Emoji.CREDIT_CARD + " Карточка",
                                    "donate " + sum + " " + YandexMoneyPaymentType.AC.name()))
                    )
                    .build();
            getTelegramApiClient().sendMessage(message);
            return;
        }

        YandexMoneyPaymentType yandexMoneyPaymentType = YandexMoneyPaymentType.valueOf(paymentTypeParam);
        YandexMoneyPaymentProvider paymentProvider = new YandexMoneyPaymentProvider();
        YandexMoneyPaymentParameters parameters = new YandexMoneyPaymentParameters(ctx.getFrom().getId(), sum);
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
            Message message = Message.safeMessageBuilder()
                    .text("<b>Сумма:</b> " + sum + " руб.\n<b>Способ:</b> " + paymentDescription)
                    .chatId(new ChatId(ctx.getChatId()))
                    .parseMode(ParseMode.HTML)
                    .replyMarkup(InlineKeyboardMarkup.oneButton(urlButton("Перевести", paymentUrl)))
                    .build();
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

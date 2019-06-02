package me.nizheg.telegram.bot.chgk.util;

import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.domain.WarningOperation;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.util.Emoji;

/**
 * @author Nikolay Zhegalin
 */
@Component
public class WarningSender implements WarningOperation {

    private final Supplier<TelegramApiClient> telegramApiClientSupplier;

    public WarningSender(Supplier<TelegramApiClient> telegramApiClientSupplier) {
        this.telegramApiClientSupplier = telegramApiClientSupplier;
    }

    @Override
    public void sendTimeWarning(Chat chat, int seconds) {
        getTelegramApiClient().sendMessage(
                Message.safeMessageBuilder()
                        .text(Emoji.HOURGLASS + " <b>Осталось " + seconds + " с.</b>")
                        .chatId(new ChatId(chat.getId()))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(false)
                        .build());
    }

    private TelegramApiClient getTelegramApiClient() {
        return telegramApiClientSupplier.get();
    }
}

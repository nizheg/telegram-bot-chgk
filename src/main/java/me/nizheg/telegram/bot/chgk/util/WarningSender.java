package me.nizheg.telegram.bot.chgk.util;

import org.springframework.stereotype.Component;

import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.domain.WarningOperation;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.util.Emoji;

/**
 * @author Nikolay Zhegalin
 */
@Component
public class WarningSender implements WarningOperation {

    private final TelegramApiClient telegramApiClient;

    public WarningSender(TelegramApiClient telegramApiClient) {this.telegramApiClient = telegramApiClient;}

    @Override
    public void sendTimeWarning(Chat chat, int seconds) {
        telegramApiClient.sendMessage(
                new Message(Emoji.HOURGLASS + " <b>Осталось " + seconds + " с.</b>", chat.getId(), ParseMode.HTML,
                        false));
    }
}

package me.nizheg.telegram.bot.chgk.util;

import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.domain.WarningOperation;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.util.Emoji;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nikolay Zhegalin
 */
@Component
public class WarningSender implements WarningOperation {

    @Autowired
    private TelegramApiClient telegramApiClient;

    @Override
    public void sendTimeWarning(Chat chat, int seconds) {
        telegramApiClient.sendMessage(new Message(Emoji.HOURGLASS + " <b>Осталось " + seconds + " с.</b>", chat.getId(), ParseMode.HTML, false));
    }
}

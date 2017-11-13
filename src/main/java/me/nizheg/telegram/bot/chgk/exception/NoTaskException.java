package me.nizheg.telegram.bot.chgk.exception;

import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.model.ParseMode;
import me.nizheg.telegram.service.param.Message;
import me.nizheg.telegram.util.TelegramApiUtil;

/**
 * @author Nikolay Zhegalin
 */
public class NoTaskException extends CommandException {

    public NoTaskException(Long chatId) {
        super(new Message("<i>Активных вопросов не обнаружено.</i>", chatId, ParseMode.HTML, null, null, TelegramApiUtil.createInlineButtonMarkup(
                "Получить вопрос", "next")));
    }

}

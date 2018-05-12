package me.nizheg.telegram.bot.chgk.service;

import me.nizheg.telegram.bot.chgk.dto.FeedbackResult;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;

/**

 *
 * @author Nikolay Zhegalin
 */
public interface FeedbackService {
    FeedbackResult registerFeedback(TelegramUser telegramUser, String text);
}

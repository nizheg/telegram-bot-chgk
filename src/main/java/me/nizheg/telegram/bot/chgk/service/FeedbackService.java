package me.nizheg.telegram.bot.chgk.service;

import me.nizheg.telegram.bot.chgk.dto.FeedbackResult;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public interface FeedbackService {
    FeedbackResult registerFeedback(TelegramUser telegramUser, String text);
}

package me.nizheg.chgk.service;

import me.nizheg.chgk.dto.FeedbackResult;
import me.nizheg.chgk.dto.TelegramUser;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public interface FeedbackService {
    FeedbackResult registerFeedback(TelegramUser telegramUser, String text);
}

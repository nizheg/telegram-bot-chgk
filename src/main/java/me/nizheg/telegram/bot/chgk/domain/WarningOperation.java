package me.nizheg.telegram.bot.chgk.domain;

import me.nizheg.telegram.bot.chgk.dto.Chat;

/**
 * @author Nikolay Zhegalin
 */
public interface WarningOperation {
    void sendTimeWarning(Chat chat, int seconds);
}

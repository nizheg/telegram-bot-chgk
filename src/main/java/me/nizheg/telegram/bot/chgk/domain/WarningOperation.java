package me.nizheg.telegram.bot.chgk.domain;

import me.nizheg.telegram.bot.chgk.dto.Chat;

/**
 * @author Nikolay Zhegalin
 */
public interface WarningOperation {
    public void sendTimeWarning(Chat chat, int seconds);
}

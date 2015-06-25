package me.nizheg.chgk.domain;

import me.nizheg.chgk.dto.Chat;

/**
 * @author Nikolay Zhegalin
 */
public interface WarningOperation {
    public void sendTimeWarning(Chat chat, int seconds);
}

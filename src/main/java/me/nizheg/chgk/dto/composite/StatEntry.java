package me.nizheg.chgk.dto.composite;

import me.nizheg.chgk.dto.TelegramUser;

/**
 * @author Nikolay Zhegalin
 */
public class StatEntry {
    private TelegramUser telegramUser;
    private Long count;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public TelegramUser getTelegramUser() {
        return telegramUser;
    }

    public void setTelegramUser(TelegramUser telegramUser) {
        this.telegramUser = telegramUser;
    }
}

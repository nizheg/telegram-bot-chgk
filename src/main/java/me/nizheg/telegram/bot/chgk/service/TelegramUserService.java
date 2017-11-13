package me.nizheg.telegram.bot.chgk.service;

import me.nizheg.telegram.bot.chgk.dto.TelegramUser;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public interface TelegramUserService {
    boolean isExist(Long id);

    TelegramUser getTelegramUser(Long id);

    TelegramUser createOrUpdate(TelegramUser telegramUser);

    TelegramUser getByUsername(String username);

}

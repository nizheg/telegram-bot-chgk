package me.nizheg.chgk.service;

import me.nizheg.chgk.dto.TelegramUser;

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

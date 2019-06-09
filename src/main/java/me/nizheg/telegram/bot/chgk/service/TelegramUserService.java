package me.nizheg.telegram.bot.chgk.service;

import me.nizheg.telegram.bot.chgk.dto.Role;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;

/**
 * @author Nikolay Zhegalin
 */
public interface TelegramUserService {

    boolean isExist(long id);

    TelegramUser getTelegramUser(long id);

    TelegramUser createOrUpdate(TelegramUser telegramUser);

    TelegramUser getByUsername(String username);

    boolean userHasRole(long telegramUserId, Role role);

}

package me.nizheg.telegram.bot.chgk.repository;

import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public interface TelegramUserDao {
    boolean isExist(Long id);

    TelegramUser create(TelegramUser telegramUser) throws DuplicationException;

    TelegramUser read(Long id);

    TelegramUser update(TelegramUser telegramUser);

    TelegramUser getByUsername(String username);

}

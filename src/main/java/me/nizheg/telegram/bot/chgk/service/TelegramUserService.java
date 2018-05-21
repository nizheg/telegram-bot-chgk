package me.nizheg.telegram.bot.chgk.service;

import java.util.Date;
import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.Role;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;

/**
 * @author Nikolay Zhegalin
 */
public interface TelegramUserService {

    boolean isExist(Long id);

    TelegramUser getTelegramUser(Long id);

    TelegramUser createOrUpdate(TelegramUser telegramUser);

    TelegramUser getByUsername(String username);

    boolean userHasRole(Long telegramUserId, Role role);

    List<Role> getRolesOfUser(Long telegramUserId);

    void assignRole(Role role, Long telegramUserId);

    void assignRoleTillTime(Role role, Long telegramUserId, Date date);

    void revokeRole(Role role, Long telegramUserId);

    void cleanExpiredRoles();
}

package me.nizheg.telegram.bot.chgk.repository;

import java.util.Date;
import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.Role;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;


/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public interface UserRoleDao {
    boolean userHasRole(Long telegramUserId, Role role);

    List<Role> readRolesOfUser(Long telegramUserId);

    void assignRole(Role role, Long telegramUserId) throws DuplicationException;

    void assignRoleTillTime(Role role, Long telegramUserId, Date date) throws DuplicationException;

    void revokeRole(Role role, Long telegramUserId);

    void deleteExpiredRoles();
}

package me.nizheg.telegram.bot.chgk.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.Role;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;
import me.nizheg.telegram.bot.chgk.repository.TelegramUserDao;
import me.nizheg.telegram.bot.chgk.repository.UserRoleDao;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;

/**
 * @author Nikolay Zhegalin
 */
@Service
public class TelegramUserServiceImpl implements TelegramUserService {

    private final Log logger = LogFactory.getLog(getClass());
    private final TelegramUserDao telegramUserDao;
    private final UserRoleDao userRoleDao;

    public TelegramUserServiceImpl(
            TelegramUserDao telegramUserDao,
            UserRoleDao userRoleDao) {
        this.telegramUserDao = telegramUserDao;
        this.userRoleDao = userRoleDao;
    }

    @Override
    public boolean isExist(Long id) {
        return telegramUserDao.isExist(id);
    }

    @Override
    public TelegramUser getTelegramUser(Long id) {
        return telegramUserDao.read(id);
    }

    @Transactional
    @Override
    public TelegramUser createOrUpdate(TelegramUser telegramUser) {
        try {
            if (telegramUserDao.isExist(telegramUser.getId())) {
                return telegramUserDao.update(telegramUser);
            } else {
                return telegramUserDao.create(telegramUser);
            }
        } catch (DuplicationException ex) {
            logger.error("Try once again after error", ex);
            return createOrUpdate(telegramUser);
        }
    }

    @Override
    public TelegramUser getByUsername(String username) {
        return telegramUserDao.getByUsername(username);
    }

    @Override
    public boolean userHasRole(Long telegramUserId, Role role) {
        return userRoleDao.userHasRole(telegramUserId, role);
    }

    @Override
    public List<Role> getRolesOfUser(Long telegramUserId) {
        return userRoleDao.readRolesOfUser(telegramUserId);
    }

    @Override
    public void assignRole(Role role, Long telegramUserId) {
        userRoleDao.revokeRole(role, telegramUserId);
        try {
            userRoleDao.assignRole(role, telegramUserId);
        } catch (DuplicationException ex) {
            logger.error("Try once again after error", ex);
            assignRole(role, telegramUserId);
        }
    }

    @Override
    public void assignRoleTillTime(Role role, Long telegramUserId, Date date) {
        userRoleDao.revokeRole(role, telegramUserId);
        try {
            userRoleDao.assignRoleTillTime(role, telegramUserId, date);
        } catch (DuplicationException ex) {
            logger.error("Try once again after error", ex);
            assignRoleTillTime(role, telegramUserId, date);
        }
    }

    @Override
    public void revokeRole(Role role, Long telegramUserId) {
        userRoleDao.revokeRole(role, telegramUserId);
    }

    @Override
    public void cleanExpiredRoles() {
        userRoleDao.deleteExpiredRoles();
    }

}

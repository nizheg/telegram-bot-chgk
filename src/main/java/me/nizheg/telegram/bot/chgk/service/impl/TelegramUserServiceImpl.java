package me.nizheg.telegram.bot.chgk.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.cache.CacheManager;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
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
public class TelegramUserServiceImpl extends
        me.nizheg.telegram.bot.starter.service.impl.TelegramUserServiceImpl
        implements TelegramUserService {

    private final Log logger = LogFactory.getLog(getClass());
    private final TelegramUserDao telegramUserDao;
    private final UserRoleDao userRoleDao;

    public TelegramUserServiceImpl(
            @NonNull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @Autowired(required = false) @Nullable CacheManager cacheManager,
            @NonNull TelegramUserDao telegramUserDao,
            @NonNull UserRoleDao userRoleDao) {
        super(telegramApiClientSupplier, cacheManager);
        this.telegramUserDao = telegramUserDao;
        this.userRoleDao = userRoleDao;
    }

    @Override
    public boolean isExist(long id) {
        return telegramUserDao.isExist(id);
    }

    @Override
    public TelegramUser getTelegramUser(long id) {
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
    public boolean userHasRole(long telegramUserId, Role role) {
        return userRoleDao.userHasRole(telegramUserId, role);
    }
}

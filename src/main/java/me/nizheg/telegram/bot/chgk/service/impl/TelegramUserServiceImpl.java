package me.nizheg.telegram.bot.chgk.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;
import me.nizheg.telegram.bot.chgk.repository.TelegramUserDao;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;

/**
 * @author Nikolay Zhegalin
 */
@Service
public class TelegramUserServiceImpl implements TelegramUserService {

    private final Log logger = LogFactory.getLog(getClass());
    private final TelegramUserDao telegramUserDao;

    public TelegramUserServiceImpl(TelegramUserDao telegramUserDao) {this.telegramUserDao = telegramUserDao;}

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

}

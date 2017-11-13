package me.nizheg.telegram.bot.chgk.service.impl;

import me.nizheg.telegram.bot.chgk.dto.ScheduledOperation;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;
import me.nizheg.telegram.bot.chgk.repository.ScheduledOperationDao;
import me.nizheg.telegram.bot.chgk.service.ScheduledOperationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Nikolay Zhegalin
 */
@Service
public class ScheduledOperationServiceImpl implements ScheduledOperationService {
    @Autowired
    private ScheduledOperationDao scheduledOperationDao;

    @Override
    public ScheduledOperation create(ScheduledOperation operation) throws DuplicationException {
        return scheduledOperationDao.create(operation);
    }

    @Override
    public ScheduledOperation getByChatId(long chatId) {
        return scheduledOperationDao.getByChatId(chatId);
    }

    @Override
    public void deleteByChatId(long chatId) {
        scheduledOperationDao.deleteByChatId(chatId);
    }

}

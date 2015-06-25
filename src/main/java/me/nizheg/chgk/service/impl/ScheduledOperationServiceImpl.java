package me.nizheg.chgk.service.impl;

import me.nizheg.chgk.dto.ScheduledOperation;
import me.nizheg.chgk.exception.DuplicationException;
import me.nizheg.chgk.repository.ScheduledOperationDao;
import me.nizheg.chgk.service.ScheduledOperationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

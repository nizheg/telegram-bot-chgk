package me.nizheg.telegram.bot.chgk.service;

import me.nizheg.telegram.bot.chgk.dto.ScheduledOperation;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;

/**
 * @author Nikolay Zhegalin
 */
public interface ScheduledOperationService {

    ScheduledOperation create(ScheduledOperation operation) throws DuplicationException;

    void deleteByChatId(long chatId);

    ScheduledOperation getByChatId(long chatId);

}

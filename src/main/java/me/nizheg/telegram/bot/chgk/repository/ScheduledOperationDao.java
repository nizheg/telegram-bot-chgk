package me.nizheg.telegram.bot.chgk.repository;

import me.nizheg.telegram.bot.chgk.dto.ScheduledOperation;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;

/**
 * @author Nikolay Zhegalin
 */
public interface ScheduledOperationDao {
    ScheduledOperation create(ScheduledOperation operation) throws DuplicationException;

    ScheduledOperation getByChatId(long chatId);

    void deleteByChatId(long chatId);
}

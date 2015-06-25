package me.nizheg.chgk.repository;

import java.util.List;

import me.nizheg.chgk.dto.ScheduledOperation;
import me.nizheg.chgk.exception.DuplicationException;

/**
 * @author Nikolay Zhegalin
 */
public interface ScheduledOperationDao {
    ScheduledOperation create(ScheduledOperation operation) throws DuplicationException;

    ScheduledOperation getByChatId(long chatId);

    void deleteByChatId(long chatId);
}

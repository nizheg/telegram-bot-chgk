package me.nizheg.chgk.service;

import java.util.List;

import me.nizheg.chgk.dto.ScheduledOperation;
import me.nizheg.chgk.exception.DuplicationException;

/**
 * @author Nikolay Zhegalin
 */
public interface ScheduledOperationService {

    ScheduledOperation create(ScheduledOperation operation) throws DuplicationException;

    void deleteByChatId(long chatId);

    ScheduledOperation getByChatId(long chatId);

}

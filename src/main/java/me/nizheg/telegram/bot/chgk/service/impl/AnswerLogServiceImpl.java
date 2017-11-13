package me.nizheg.telegram.bot.chgk.service.impl;

import java.util.Date;
import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.AnswerLog;
import me.nizheg.telegram.bot.chgk.dto.composite.StatEntry;
import me.nizheg.telegram.bot.chgk.repository.AnswerLogDao;
import me.nizheg.telegram.bot.chgk.repository.param.StatSearchParams;
import me.nizheg.telegram.bot.chgk.service.AnswerLogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Nikolay Zhegalin
 */
@Service
public class AnswerLogServiceImpl implements AnswerLogService {

    @Autowired
    private AnswerLogDao answerLogDao;

    @Override
    public AnswerLog create(AnswerLog answerLog) {
        if (answerLog.getTime() == null) {
            answerLog.setTime(new Date());
        }
        return answerLogDao.create(answerLog);
    }

    @Override
    public AnswerLog getByTaskAndChat(Long taskId, Long chatId) {
        return answerLogDao.getByTaskAndChat(taskId, chatId);
    }

    @Override
    public boolean isExistByTaskAndChat(Long taskId, Long chatId) {
        return answerLogDao.isExistByTaskAndChat(taskId, chatId);
    }

    @Override
    public List<StatEntry> getStatForChat(Long chatId, StatSearchParams params) {
        if (params == null) {
            params = new StatSearchParams();
        }
        return answerLogDao.getStatForChat(chatId, params);
    }

    @Override
    public List<StatEntry> getStatForChatUser(Long chatId, Long userId, String othersUsername) {
        return answerLogDao.getStatForChatUser(chatId, userId, othersUsername);
    }

    @Override
    public List<StatEntry> getStatForChatUserForTournament(Long chatId, Long userId, String othersUsername, Long tournamentId) {
        return answerLogDao.getStatForChatUserForTournament(chatId, userId, othersUsername, tournamentId);
    }
}

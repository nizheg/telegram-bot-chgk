package me.nizheg.chgk.service;

import java.util.List;

import me.nizheg.chgk.dto.AnswerLog;
import me.nizheg.chgk.dto.composite.StatEntry;
import me.nizheg.chgk.repository.param.StatSearchParams;

/**
 * @author Nikolay Zhegalin
 */
public interface AnswerLogService {
    AnswerLog create(AnswerLog answerLog);

    AnswerLog getByTaskAndChat(Long taskId, Long chatId);

    boolean isExistByTaskAndChat(Long taskId, Long chatId);

    List<StatEntry> getStatForChat(Long chatId, StatSearchParams params);

    List<StatEntry> getStatForChatUser(Long chatId, Long userId, String othersUsername);

    List<StatEntry> getStatForChatUserForTournament(Long chatId, Long userId, String othersUsername, Long tournamentId);
}

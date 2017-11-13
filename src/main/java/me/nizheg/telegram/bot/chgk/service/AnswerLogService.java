package me.nizheg.telegram.bot.chgk.service;

import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.AnswerLog;
import me.nizheg.telegram.bot.chgk.dto.composite.StatEntry;
import me.nizheg.telegram.bot.chgk.repository.param.StatSearchParams;

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

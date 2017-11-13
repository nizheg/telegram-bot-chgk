package me.nizheg.telegram.bot.chgk.repository;

import me.nizheg.telegram.bot.chgk.dto.AnswerLog;
import me.nizheg.telegram.bot.chgk.dto.composite.StatEntry;
import me.nizheg.telegram.bot.chgk.repository.param.StatSearchParams;

import java.util.List;

/**
 * @author Nikolay Zhegalin
 */
public interface AnswerLogDao {
    AnswerLog create(AnswerLog answerLog);

    AnswerLog getByTaskAndChat(Long taskId, Long chatId);

    boolean isExistByTaskAndChat(Long taskId, Long chatId);

    List<StatEntry> getStatForChat(Long chatId, StatSearchParams params);

    List<StatEntry> getStatForChatUser(Long chatId, Long userId, String othersUsername);

    List<StatEntry> getStatForChatUserForTournament(Long chatId, Long userId, String othersUsername, Long tournamentId);

    void copy(Long fromChatId, Long toChatId);
}

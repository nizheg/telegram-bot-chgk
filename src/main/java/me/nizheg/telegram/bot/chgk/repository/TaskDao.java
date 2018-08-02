package me.nizheg.telegram.bot.chgk.repository;

import java.time.OffsetDateTime;
import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.Category;
import me.nizheg.telegram.bot.chgk.dto.LightTask;
import me.nizheg.telegram.bot.chgk.dto.UsageStat;

public interface TaskDao {

    LightTask create(LightTask task);

    LightTask getById(Long id);

    LightTask update(LightTask task);

    void delete(Long id);

    List<LightTask> getCollection();

    void setUsedByChat(Long id, Long chatId);

    LightTask getLastUsedTask(Long chatId);

    OffsetDateTime getUsageTime(Long taskId, Long chatId);

    LightTask getUnusedByChat(Long chatId, Category category);

    LightTask getNextTaskInTournament(long tournamentId, int currentTourNumber, int currentNumberInTour);

    List<LightTask> getByStatus(LightTask.Status status);

    List<LightTask> getByText(String text);

    UsageStat getUsageStatForChatByChatId(long chatId, Category category);

    UsageStat getUsageStatForChatByTournament(long chatId, long tournamentId);

    boolean isExist(String text);

    void addCategory(Long taskId, String categoryId);

    void removeCategory(Long taskId, String categoryId);

    void copyUsedTasks(Long fromChatId, Long toChatId);

    void deleteUsedTasks(Long chatId);

    List<LightTask> getByTour(long id);

    void updateStatus(List<Long> taskIds, LightTask.Status fromStatus, LightTask.Status toStatus);

    /**
     * Transaction required
     */
    int archiveTasks();
}

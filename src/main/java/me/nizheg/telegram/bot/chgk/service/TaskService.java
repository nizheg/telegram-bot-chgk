package me.nizheg.telegram.bot.chgk.service;

import java.util.Date;
import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.Category;
import me.nizheg.telegram.bot.chgk.dto.LightTask;
import me.nizheg.telegram.bot.chgk.dto.LightTask.Status;
import me.nizheg.telegram.bot.chgk.dto.UsageStat;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.dto.composite.Tournament;

/**

 *
 * @author Nikolay Zhegalin
 */
public interface TaskService {

    LightTask create(LightTask task);

    LightTask read(Long id);

    LightTask update(LightTask task);

    void delete(Long id);

    List<LightTask> getCollection();

    void setTaskUsed(Long id, Long chatId);

    Date getUsageTime(Long taskId, Long chatId);

    LightTask getUnusedByChatTask(Long chatId, Category category);

    List<LightTask> getByStatus(LightTask.Status status);

    List<LightTask> getByText(String text);

    LightTask changeStatus(Long taskId, LightTask.Status status);

    void changeStatus(List<Long> taskIds, Status fromStatus, Status toStatus);

    boolean isExist(String text);

    void addCategory(Long taskId, String categoryId);

    void removeCategory(Long taskId, String categoryId);

    Task createCompositeTask(Long id);

    Task createCompositeTask(LightTask lightTask);

    LightTask getNextTaskInTournament(Tournament currentTournament, Task currentTask);

    UsageStat getUsageStatForChat(long chatId, Category category);

    UsageStat getUsageStatForChatByTournament(long chatId, Tournament tournament);

    LightTask getLastUsedTask(long chatId);

    int archiveTasks();
}

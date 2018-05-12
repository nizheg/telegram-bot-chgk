package me.nizheg.telegram.bot.chgk.service;

import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.LightTask;

/**

 *
 * @author Nikolay Zhegalin
 */
public interface TaskLoaderService {
    List<LightTask> loadTasks(int complexity);

    List<LightTask> loadTour(String id);
}

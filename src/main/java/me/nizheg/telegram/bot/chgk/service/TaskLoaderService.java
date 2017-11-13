package me.nizheg.telegram.bot.chgk.service;

import me.nizheg.telegram.bot.chgk.dto.LightTask;

import java.util.List;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public interface TaskLoaderService {
    List<LightTask> loadTasks(int complexity);

    List<LightTask> loadTour(String id);
}

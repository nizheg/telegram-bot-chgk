package me.nizheg.telegram.bot.chgk.service;

import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.composite.Task;

/**
 * @author Nikolay Zhegalin
 */
public interface TaskLoaderService {

    List<Task> loadTasks(int complexity);

    List<Task> loadTour(String id);
}

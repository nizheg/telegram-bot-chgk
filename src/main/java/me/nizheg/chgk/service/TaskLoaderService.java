package me.nizheg.chgk.service;

import me.nizheg.chgk.dto.LightTask;

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

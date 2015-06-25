package me.nizheg.chgk.service;

import java.util.List;

import me.nizheg.chgk.dto.LightTask;
import me.nizheg.chgk.dto.LightTour;

/**
 * @author Nikolay Zhegalin
 */
public interface TourLoaderService {

    List<LightTask> loadQuestionsOfTour(String id);

    LightTour loadTour(String id);
}

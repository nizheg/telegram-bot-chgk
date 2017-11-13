package me.nizheg.telegram.bot.chgk.service;

import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.LightTask;
import me.nizheg.telegram.bot.chgk.dto.LightTour;

/**
 * @author Nikolay Zhegalin
 */
public interface TourLoaderService {

    List<LightTask> loadQuestionsOfTour(String id);

    LightTour loadTour(String id);
}

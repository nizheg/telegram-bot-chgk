package me.nizheg.telegram.bot.chgk.service;

import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.LightTour;
import me.nizheg.telegram.bot.chgk.dto.composite.LightTourWithStat;

/**
 * @author Nikolay Zhegalin
 */
public interface TourService {
    boolean isTourExists(long tourId);

    LightTour create(LightTour lightTour);

    LightTour read(long id);

    LightTour createCompositeTour(long id);

    LightTour createCompositeTour(LightTour lightTour);

    LightTour changeStatus(long tourId, LightTour.Status status);

    List<LightTour> getByTypeAndStatus(LightTour.Type type, LightTour.Status status);

    List<LightTourWithStat> getPublishedTournamentsWithStatForChat(long chatId, int limit, int offset);

    int getPublishedTournamentsCount();

    List<LightTour> getByType(LightTour.Type type);
}

package me.nizheg.chgk.service;

import java.util.List;

import me.nizheg.chgk.dto.LightTour;
import me.nizheg.chgk.dto.LightTour.Status;
import me.nizheg.chgk.dto.LightTour.Type;
import me.nizheg.chgk.dto.composite.LightTourWithStat;

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

    List<LightTour> getByTypeAndStatus(Type type, Status status);

    List<LightTourWithStat> getPublishedTournamentsWithStatForChat(long chatId, int limit, int offset);

    int getPublishedTournamentsCount();

    List<LightTour> getByType(Type type);
}

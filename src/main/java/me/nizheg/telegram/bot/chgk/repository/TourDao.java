package me.nizheg.telegram.bot.chgk.repository;

import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.LightTour;
import me.nizheg.telegram.bot.chgk.dto.LightTour.Status;
import me.nizheg.telegram.bot.chgk.dto.LightTour.Type;
import me.nizheg.telegram.bot.chgk.dto.PageResult;
import me.nizheg.telegram.bot.chgk.dto.composite.LightTourWithStat;

/**
 * @author Nikolay Zhegalin
 */
public interface TourDao {

    boolean isTourExists(long tourId);

    LightTour create(LightTour lightTour);

    LightTour update(LightTour tour);

    LightTour getById(long id);

    List<LightTour> getByParentTour(long tourId);

    List<LightTour> getByTypeAndStatus(Type type, Status status);

    PageResult<LightTour> getPublishedTournamentsByQuery(String query, int limit, int offset);

    List<LightTourWithStat> getPublishedTournamentsWithStatForChat(long chatId, int limit, int offset);

    int getPublishedTournamentsCount();

}

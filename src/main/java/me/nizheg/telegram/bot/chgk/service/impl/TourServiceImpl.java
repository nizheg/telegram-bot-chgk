package me.nizheg.telegram.bot.chgk.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import me.nizheg.telegram.bot.chgk.dto.LightTask;
import me.nizheg.telegram.bot.chgk.dto.LightTour;
import me.nizheg.telegram.bot.chgk.dto.LightTour.Status;
import me.nizheg.telegram.bot.chgk.dto.LightTour.Type;
import me.nizheg.telegram.bot.chgk.dto.composite.LightTourWithStat;
import me.nizheg.telegram.bot.chgk.dto.composite.Tour;
import me.nizheg.telegram.bot.chgk.dto.composite.TourGroup;
import me.nizheg.telegram.bot.chgk.dto.composite.Tournament;
import me.nizheg.telegram.bot.chgk.repository.TaskDao;
import me.nizheg.telegram.bot.chgk.repository.TourDao;
import me.nizheg.telegram.bot.chgk.service.TourService;

/**
 * @author Nikolay Zhegalin
 */
@Service
public class TourServiceImpl implements TourService {

    private final TourDao tourDao;
    private final TaskDao taskDao;

    public TourServiceImpl(TourDao tourDao, TaskDao taskDao) {
        this.tourDao = tourDao;
        this.taskDao = taskDao;
    }

    @Override
    public boolean isTourExists(long tourId) {
        return tourDao.isTourExists(tourId);
    }

    @Override
    public LightTour create(LightTour lightTour) {
        lightTour.setStatus(LightTour.Status.NEW);
        return tourDao.create(lightTour);
    }

    @Override
    public LightTour read(long id) {
        return tourDao.getById(id);
    }

    @Override
    public List<LightTour> getByTypeAndStatus(Type type, Status status) {
        return tourDao.getByTypeAndStatus(type, status);
    }

    @Override
    public List<LightTourWithStat> getPublishedTournamentsWithStatForChat(long chatId, int limit, int offset) {
        return tourDao.getPublishedTournamentsWithStatForChat(chatId, limit, offset);
    }

    @Override
    public int getPublishedTournamentsCount() {
        return tourDao.getPublishedTournamentsCount();
    }

    @Override
    public List<LightTour> getByType(Type type) {
        return tourDao.getByTypeAndStatus(type, null);
    }

    @Transactional
    @Override
    public LightTour createCompositeTour(long id) {
        LightTour lightTour = read(id);
        return createCompositeTour(lightTour);
    }

    @Transactional
    @Override
    @Nullable
    public LightTour createCompositeTour(LightTour lightTour) {
        if (lightTour == null) {
            return null;
        }
        if (lightTour.getType() != null) {
            switch (lightTour.getType()) {
                case TOUR:
                    Tour tour = new Tour(lightTour);
                    if (lightTour.getParentTourId() != null) {
                        LightTour parentTour = read(lightTour.getParentTourId());
                        tour.setParentTour(parentTour);
                    }
                    tour.setTasks(taskDao.getByTour(tour.getId()));
                    return tour;
                case TOURNAMENT:
                    Tournament tournament = new Tournament(lightTour);
                    if (lightTour.getParentTourId() != null) {
                        LightTour parentTour = read(lightTour.getParentTourId());
                        tournament.setParentTour(parentTour);
                    }
                    tournament.setChildTours(tourDao.getByParentTour(lightTour.getId()));
                    return tournament;
                case TOURNAMENT_GROUP:
                    TourGroup tourGroup = new TourGroup(lightTour);
                    if (lightTour.getParentTourId() != null) {
                        LightTour parentTour = read(lightTour.getParentTourId());
                        tourGroup.setParentTour(parentTour);
                    }
                    tourGroup.setChildTours(tourDao.getByParentTour(lightTour.getId()));
                    return tourGroup;
            }
        }
        return lightTour;
    }

    @Transactional
    @Override
    public LightTour changeStatus(long tourId, LightTour.Status status) {
        LightTour lightTour = read(tourId);
        if (status == null || lightTour == null) {
            return lightTour;
        }
        if (status == Status.PUBLISHED) {
            prePublish(lightTour);
        }
        lightTour.setStatus(status);
        return tourDao.update(lightTour);
    }

    private void prePublish(LightTour lightTour) {
        LightTour compositeTour = createCompositeTour(lightTour);
        if (compositeTour instanceof Tournament) {
            List<Long> taskIds = new LinkedList<>();
            for (LightTour childTour : ((Tournament) compositeTour).getChildTours()) {
                if (LightTour.Status.NEW.equals(childTour.getStatus())) {
                    throw new IllegalStateException("Tour " + childTour.getId() + " is not published yet");
                }
                Tour tour = (Tour) createCompositeTour(childTour);
                if (tour != null) {
                    for (LightTask lightTask : tour.getTasks()) {
                        if (LightTask.Status.NEW.equals(lightTask.getStatus())) {
                            throw new IllegalStateException("Task " + lightTask.getId() + " is not published yet");
                        }
                        taskIds.add(lightTask.getId());
                    }
                }
                if (!taskIds.isEmpty()) {
                    taskDao.updateStatus(taskIds, LightTask.Status.PUBLISH_READY, LightTask.Status.PUBLISHED);
                }
            }
        } else if (compositeTour instanceof Tour) {
            for (LightTask lightTask : ((Tour) compositeTour).getTasks()) {
                if (LightTask.Status.NEW.equals(lightTask.getStatus())) {
                    throw new IllegalStateException("Task " + lightTask.getId() + " is not published yet");
                }
            }
        }
    }

}

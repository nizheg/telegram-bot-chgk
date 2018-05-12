package me.nizheg.telegram.bot.chgk.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import info.chgk.db.service.TasksImporter;
import info.chgk.db.xml.Question;
import info.chgk.db.xml.Tournament;
import me.nizheg.telegram.bot.chgk.dto.LightTask;
import me.nizheg.telegram.bot.chgk.dto.LightTour;
import me.nizheg.telegram.bot.chgk.service.TaskLoaderService;
import me.nizheg.telegram.bot.chgk.service.TourLoaderService;
import me.nizheg.telegram.bot.chgk.service.TourService;

/**
 * @author Nikolay Zhegalin
 */
@Service
public class TourLoaderServiceImpl implements TourLoaderService {

    private final TasksImporter tasksImporter;
    private final TourService tourService;
    private final TaskLoaderService taskLoaderService;

    private final Map<String, LightTour.Type> typesMapping = new HashMap<>();

    {
        typesMapping.put("Ч", LightTour.Type.TOURNAMENT);
        typesMapping.put("Г", LightTour.Type.TOURNAMENT_GROUP);
        typesMapping.put("Т", LightTour.Type.TOUR);
    }

    public TourLoaderServiceImpl(
            TasksImporter tasksImporter,
            TourService tourService, TaskLoaderService taskLoaderService) {
        this.tasksImporter = tasksImporter;
        this.tourService = tourService;
        this.taskLoaderService = taskLoaderService;
    }

    @Override
    @Transactional
    public List<LightTask> loadQuestionsOfTour(String id) {
        Tournament tournament = tasksImporter.importTour(id);
        createTourWithParents(tournament);
        List<LightTask> loadedTasks = new LinkedList<>();
        if (!tournament.getQuestion().isEmpty()) {
            loadedTasks.addAll(taskLoaderService.loadTour(id));
        } else {
            for (info.chgk.db.xml.Tour tour : tournament.getTour()) {
                loadedTasks.addAll(loadQuestionsOfTour(String.valueOf(tour.getId())));
            }
        }
        return loadedTasks;
    }

    @Override
    @Transactional
    public LightTour loadTour(String id) {
        Tournament tournament = tasksImporter.importTour(id);
        createTourWithParents(tournament);
        return tourService.read(tournament.getId());
    }

    private void createTourWithParents(Tournament tournament) {
        if (tournament.getParentId() != null) {
            Tournament parentTournament = tasksImporter.importTour(tournament.getParentId().toString());
            createTourWithParents(parentTournament);
        }
        createTour(tournament);
    }

    private void createTour(Tournament tournament) {
        if (!tourService.isTourExists(tournament.getId())) {
            LightTour lightTour = new LightTour();
            lightTour.setId(tournament.getId());
            lightTour.setParentTourId(tournament.getParentId());
            lightTour.setTitle(tournament.getTitle());
            lightTour.setNumber(tournament.getNumber());
            XMLGregorianCalendar playedAt = tournament.getPlayedAt();
            if (playedAt != null) {
                lightTour.setPlayedAt(playedAt.toGregorianCalendar().getTime());
            }

            LightTour.Type type = typesMapping.get(tournament.getType());
            lightTour.setType(type);
            tourService.create(lightTour);
            if (type != null && type == LightTour.Type.TOURNAMENT && !tournament.getQuestion().isEmpty()) {
                Set<Long> childTourIds = new HashSet<>();
                for (Question question : tournament.getQuestion()) {
                    childTourIds.add(question.getParentId());
                }
                for (Long childTourId : childTourIds) {
                    Tournament childTour = tasksImporter.importTour(childTourId.toString());
                    createTour(childTour);
                }
            }
        }
    }

}

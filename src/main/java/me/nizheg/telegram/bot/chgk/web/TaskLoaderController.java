package me.nizheg.telegram.bot.chgk.web;

import me.nizheg.telegram.bot.chgk.dto.LightTask;
import me.nizheg.telegram.bot.chgk.service.TaskLoaderService;
import me.nizheg.telegram.bot.chgk.service.TourLoaderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
@RestController
@RequestMapping("api/dbTask")
public class TaskLoaderController {
    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private TaskLoaderService taskLoaderService;
    @Autowired
    private TourLoaderService tourLoaderService;

    @RequestMapping(method = RequestMethod.POST)
    public List<LightTask> loadTasks(@RequestParam int complexity) {
        if (logger.isDebugEnabled()) {
            logger.debug("load tasks");
        }
        return taskLoaderService.loadTasks(complexity);
    }

    @RequestMapping(value = "/tour", method = RequestMethod.POST)
    public List<LightTask> loadTour(@RequestParam("tour_id") String tourId) {
        if (logger.isDebugEnabled()) {
            logger.debug("load tour");
        }
        return tourLoaderService.loadQuestionsOfTour(tourId);
    }
}
package me.nizheg.telegram.bot.chgk.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.LightTour;
import me.nizheg.telegram.bot.chgk.service.TourService;

/**
 * @author Nikolay Zhegalin
 */
@RestController
@RequestMapping("api/tour")
public class TourController {

    private final Log logger = LogFactory.getLog(getClass());
    @Autowired
    private TourService tourService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public LightTour read(@PathVariable Long id) {
        logger.debug("get tour " + id);
        return tourService.createCompositeTour(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<LightTour> readAll(@RequestParam(required = false) LightTour.Type type, @RequestParam(required = false) LightTour.Status status) {
        logger.debug("get tasks");
        if (status != null) {
            return tourService.getByTypeAndStatus(type, status);
        }
        return tourService.getByType(type);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public LightTour patch(@PathVariable Long id, @RequestBody StatusWrapper statusWrapper) {
        logger.debug("change status of tour " + id + " to " + statusWrapper.getStatus());
        return tourService.changeStatus(id, statusWrapper.getStatus());
    }

    public static class StatusWrapper {
        private LightTour.Status status;

        public LightTour.Status getStatus() {
            return status;
        }

        public void setStatus(LightTour.Status status) {
            this.status = status;
        }
    }
}

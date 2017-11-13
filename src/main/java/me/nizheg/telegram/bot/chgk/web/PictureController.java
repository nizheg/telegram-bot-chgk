package me.nizheg.telegram.bot.chgk.web;

import me.nizheg.telegram.bot.chgk.dto.Picture;
import me.nizheg.telegram.bot.chgk.service.PictureService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/picture")
public class PictureController {

    private final Log logger = LogFactory.getLog(getClass());
    @Autowired
    private PictureService pictureService;

    @RequestMapping(method = RequestMethod.POST)
    public Picture create(@RequestBody Picture picture) {
        if (logger.isDebugEnabled()) {
            logger.debug("create new picture");
        }
        return pictureService.create(picture);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Picture get(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("get picture with id" + id);
        }
        return pictureService.getById(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Picture update(@PathVariable Long id, @RequestBody Picture picture) {
        if (logger.isDebugEnabled()) {
            logger.debug("update answer " + id);
        }
        picture.setId(id);
        return pictureService.update(picture);
    }

}

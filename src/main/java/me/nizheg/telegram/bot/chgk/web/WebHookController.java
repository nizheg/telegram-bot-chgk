package me.nizheg.telegram.bot.chgk.web;

import me.nizheg.telegram.bot.api.model.Update;
import me.nizheg.telegram.bot.service.UpdateHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("0d2f72c055554a3d83c31744f7f451e0")
public class WebHookController {

    private final Log logger = LogFactory.getLog(getClass());
    @Autowired
    private UpdateHandler updateHandler;

    @RequestMapping(method = RequestMethod.POST)
    public void post(@RequestBody Update update) {
        logger.debug(Thread.currentThread().getName() + ": received update");
        updateHandler.handleUpdate(update);
    }
}

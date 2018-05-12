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

import me.nizheg.telegram.bot.chgk.dto.Answer;
import me.nizheg.telegram.bot.chgk.service.AnswerService;

/**

 *
 * @author Nikolay Zhegalin
 */
@RestController
@RequestMapping("api/answer")
public class AnswerController {

    private final Log logger = LogFactory.getLog(getClass());
    @Autowired
    private AnswerService answerService;

    @RequestMapping(method = RequestMethod.POST)
    public Answer create(@RequestBody Answer answer) {
        if (logger.isDebugEnabled()) {
            logger.debug("create new answer");
        }
        return answerService.create(answer);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Answer get(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("get answer with id" + id);
        }
        return answerService.read(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Answer> getAll(@RequestParam(required = false) Long taskId) {
        if (taskId != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("get answers by task " + taskId);
            }
            return answerService.getByTask(taskId);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("get all answers");
            }
            return answerService.getCollection();
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Object update(@PathVariable Long id, @RequestBody Answer answer) {
        if (logger.isDebugEnabled()) {
            logger.debug("update answer " + id);
        }
        answer.setId(id);
        return answerService.update(answer);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("delete answer " + id);
        }
        answerService.delete(id);
    }

}

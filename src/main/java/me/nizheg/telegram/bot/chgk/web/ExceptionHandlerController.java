package me.nizheg.telegram.bot.chgk.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

import me.nizheg.telegram.bot.chgk.exception.DuplicationException;
import me.nizheg.telegram.bot.chgk.exception.OperationForbiddenException;
import me.nizheg.telegram.bot.starter.web.dto.ExceptionResponse;

/**
 * @author Nikolay Zhegalin
 */
@RestController("chgkExceptionHandlerController")
@ControllerAdvice
@ResponseBody
public class ExceptionHandlerController {

    private final Log logger = LogFactory.getLog(getClass());

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(OperationForbiddenException.class)
    public ExceptionResponse forbidden(OperationForbiddenException ex) {
        logger.error(ex.getMessage(), ex);
        return new ExceptionResponse(ex.getMessage());
    }

    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicationException.class)
    public ExceptionResponse conflict(DuplicationException ex) {
        logger.error(ex.getMessage(), ex);
        return new ExceptionResponse(ex.getMessage());
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public ExceptionResponse databaseError(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return new ExceptionResponse(ex.getMessage());
    }

}

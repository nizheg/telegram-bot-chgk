package me.nizheg.chgk.web;

import me.nizheg.chgk.exception.DuplicationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.sql.SQLException;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
@Controller
@ControllerAdvice
@ResponseBody
public class ExceptionHandlerController {

    private Log logger = LogFactory.getLog(getClass());

    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicationException.class)
    public ExceptionResponse conflict(DuplicationException ex) {
        logger.error(ex.getMessage(), ex);
        return new ExceptionResponse(ex.getMessage());
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ SQLException.class, DataAccessException.class })
    public ExceptionResponse databaseError(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return new ExceptionResponse(ex.getMessage());
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ Exception.class })
    public ExceptionResponse commonError(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return new ExceptionResponse(ex.getMessage());
    }

    public static class ExceptionResponse {

        public ExceptionResponse(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        private String errorMessage;

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}

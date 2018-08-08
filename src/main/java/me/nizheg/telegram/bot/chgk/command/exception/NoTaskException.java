package me.nizheg.telegram.bot.chgk.command.exception;

import me.nizheg.telegram.bot.command.CommandException;

/**
 * @author Nikolay Zhegalin
 */
public class NoTaskException extends CommandException {

    private static final long serialVersionUID = 2L;


    public NoTaskException(Throwable cause) {
        super(cause);
    }

    public NoTaskException(String message) {
        super(message);
    }

    public NoTaskException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoTaskException() {
    }
}

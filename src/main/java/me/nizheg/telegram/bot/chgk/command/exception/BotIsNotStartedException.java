package me.nizheg.telegram.bot.chgk.command.exception;

import me.nizheg.telegram.bot.command.CommandException;

/**
 * @author Nikolay Zhegalin
 */
public class BotIsNotStartedException extends CommandException {

    private static final long serialVersionUID = 2L;

    public BotIsNotStartedException() {
    }

    public BotIsNotStartedException(Throwable cause) {
        super(cause);
    }

    public BotIsNotStartedException(String message) {
        super(message);
    }

    public BotIsNotStartedException(String message, Throwable cause) {
        super(message, cause);
    }


}

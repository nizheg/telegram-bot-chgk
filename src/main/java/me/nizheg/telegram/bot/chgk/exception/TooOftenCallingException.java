package me.nizheg.telegram.bot.chgk.exception;

/**
 * @author Nikolay Zhegalin
 */
public class TooOftenCallingException extends GameException {

    private static final long serialVersionUID = -1478841219797397715L;

    public TooOftenCallingException() {
    }

    public TooOftenCallingException(String message) {
        super(message);
    }

    public TooOftenCallingException(String message, Throwable cause) {
        super(message, cause);
    }

    public TooOftenCallingException(Throwable cause) {
        super(cause);
    }

    public TooOftenCallingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

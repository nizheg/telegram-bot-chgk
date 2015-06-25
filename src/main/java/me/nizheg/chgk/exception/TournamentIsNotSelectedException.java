package me.nizheg.chgk.exception;

/**
 * @author Nikolay Zhegalin
 */
public class TournamentIsNotSelectedException extends GameException {

    private static final long serialVersionUID = -1478841219797397715L;

    public TournamentIsNotSelectedException() {
    }

    public TournamentIsNotSelectedException(String message) {
        super(message);
    }

    public TournamentIsNotSelectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public TournamentIsNotSelectedException(Throwable cause) {
        super(cause);
    }

    public TournamentIsNotSelectedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

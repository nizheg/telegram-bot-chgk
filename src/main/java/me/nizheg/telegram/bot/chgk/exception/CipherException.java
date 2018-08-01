package me.nizheg.telegram.bot.chgk.exception;

/**
 * @author Nikolay Zhegalin
 */
public class CipherException extends Exception {

    private static final long serialVersionUID = -7448059126463292323L;

    public CipherException() {
    }

    public CipherException(String message) {
        super(message);
    }

    public CipherException(String message, Throwable cause) {
        super(message, cause);
    }

    public CipherException(Throwable cause) {
        super(cause);
    }
}

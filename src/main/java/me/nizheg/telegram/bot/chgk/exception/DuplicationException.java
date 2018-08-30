package me.nizheg.telegram.bot.chgk.exception;

/**
 * @author Nikolay Zhegalin
 */
public class DuplicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicationException(String s) {
        super(s);
    }
}

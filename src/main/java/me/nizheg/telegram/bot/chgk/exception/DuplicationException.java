package me.nizheg.telegram.bot.chgk.exception;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class DuplicationException extends RuntimeException {
    private static final long serialVersionUID = 1l;

    public DuplicationException(String s) {
        super(s);
    }
}

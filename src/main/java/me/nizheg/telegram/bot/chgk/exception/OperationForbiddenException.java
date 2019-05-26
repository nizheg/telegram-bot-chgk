package me.nizheg.telegram.bot.chgk.exception;

public class OperationForbiddenException extends RuntimeException {

    public OperationForbiddenException() {
    }

    public OperationForbiddenException(String message) {
        super(message);
    }
}

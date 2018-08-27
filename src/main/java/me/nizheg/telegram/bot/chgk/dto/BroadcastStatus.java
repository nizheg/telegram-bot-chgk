package me.nizheg.telegram.bot.chgk.dto;

/**
 * @author Nikolay Zhegalin
 */
public class BroadcastStatus {

    private Status status;
    private String message;
    private volatile int totalCount;
    private volatile int finished;

    public enum Status {
        CREATED, READY, STARTED, ERROR, CANCELLED, FINISHED
    }

    public BroadcastStatus() {
        this(Status.CREATED);
    }

    public BroadcastStatus(Status status) {
        this.status = status;
    }

    public BroadcastStatus(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public synchronized Status getStatus() {
        return status;
    }

    public synchronized void setStatus(Status status) {
        this.status = status;
    }

    public synchronized String getMessage() {
        return message;
    }

    public synchronized void setMessage(String message) {
        this.message = message;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }
}

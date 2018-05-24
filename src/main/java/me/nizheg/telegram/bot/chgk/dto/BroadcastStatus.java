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
        NOT_STARTED, REJECTED, FORWARD_INITIATED, IN_PROCESS, FINISHED, CANCELLED
    }

    public BroadcastStatus() {
        this(Status.NOT_STARTED);
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

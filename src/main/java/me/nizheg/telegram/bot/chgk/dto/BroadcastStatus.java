package me.nizheg.telegram.bot.chgk.dto;

/**

 *
 * @author Nikolay Zhegalin
 */
public class BroadcastStatus {

    private Status status;
    private String sendingMessage;
    private String errorMessage;
    private volatile int totalCount;
    private volatile int finished;

    public enum Status {
        NOT_STARTED, REJECTED, IN_PROCESS, FINISHED, CANCELLED
    }

    public BroadcastStatus() {
        this(Status.NOT_STARTED);
    }

    public BroadcastStatus(Status status) {
        this.status = status;
    }

    public BroadcastStatus(Status status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public synchronized Status getStatus() {
        return status;
    }

    public synchronized void setStatus(Status status) {
        this.status = status;
    }

    public synchronized String getErrorMessage() {
        return errorMessage;
    }

    public synchronized void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public synchronized String getSendingMessage() {
        return sendingMessage;
    }

    public synchronized void setSendingMessage(String sendingMessage) {
        this.sendingMessage = sendingMessage;
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

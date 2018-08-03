package me.nizheg.telegram.bot.chgk.dto;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * @author Nikolay Zhegalin
 */
public class ScheduledOperation implements Serializable {

    private static final long serialVersionUID = 47003547368614363L;
    private String operationId;
    private long chatId;
    private OffsetDateTime time;

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public OffsetDateTime getTime() {
        return time;
    }

    public void setTime(OffsetDateTime time) {
        this.time = time;
    }
}

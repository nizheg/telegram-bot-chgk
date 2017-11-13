package me.nizheg.telegram.bot.chgk.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Nikolay Zhegalin
 */
public class AnswerLog implements Serializable {
    private static final long serialVersionUID = -1381353124197340768L;
    private Long id;
    private Long telegramUserId;
    private Long chatId;
    private Long taskId;
    private Date time;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTelegramUserId() {
        return telegramUserId;
    }

    public void setTelegramUserId(Long telegramUserId) {
        this.telegramUserId = telegramUserId;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}

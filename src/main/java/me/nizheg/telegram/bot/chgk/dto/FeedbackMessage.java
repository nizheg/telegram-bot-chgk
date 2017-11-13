package me.nizheg.telegram.bot.chgk.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class FeedbackMessage implements Serializable {

    private static final long serialVersionUID = 1904644964992616040L;
    private Long id;
    private Long telegramUserId;
    private String message;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}

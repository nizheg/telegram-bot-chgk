package me.nizheg.telegram.bot.chgk.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class ChatError implements Serializable {

    private static final long serialVersionUID = 1L;
    private Date time;
    private Long chatId;
    private String code;
    private String description;

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

package me.nizheg.telegram.bot.chgk.dto;

import java.io.Serializable;

/**

 *
 * @author Nikolay Zhegalin
 */
public class Property implements Serializable {

    private static final long serialVersionUID = 1L;

    private String key;
    private String value;
    private Long chatId;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }
}

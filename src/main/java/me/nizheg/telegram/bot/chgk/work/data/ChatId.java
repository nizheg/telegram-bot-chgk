package me.nizheg.telegram.bot.chgk.work.data;

import lombok.NoArgsConstructor;

/**
 * @author Nikolay Zhegalin
 */
@NoArgsConstructor
public class ChatId {

    private Long chatId;
    private String username;

    public ChatId(Long chatId) {
        this.chatId = chatId;
        this.username = null;
    }

    public ChatId(String username) {
        this.chatId = null;
        this.username = username;
    }



}

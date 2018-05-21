package me.nizheg.telegram.bot.chgk.dto;

import javax.annotation.Nullable;

public class ForwardingMessage {

    @Nullable
    private Long fromChatId;
    @Nullable
    private Long messageId;
    @Nullable
    private String text;


    @Nullable
    public Long getFromChatId() {
        return fromChatId;
    }

    public void setFromChatId(@Nullable Long fromChatId) {
        this.fromChatId = fromChatId;
    }

    @Nullable
    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(@Nullable Long messageId) {
        this.messageId = messageId;
    }

    @Nullable
    public String getText() {
        return text;
    }

    public void setText(@Nullable String text) {
        this.text = text;
    }
}

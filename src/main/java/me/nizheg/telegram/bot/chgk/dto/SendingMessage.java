package me.nizheg.telegram.bot.chgk.dto;

import javax.annotation.Nullable;

public class SendingMessage {

    public static final String RECEIVER_ALL = "all";
    public static final String RECEIVER_ME = "me";

    @Nullable
    private TelegramUser sender;
    @Nullable
    private String receiver;
    @Nullable
    private String text;
    @Nullable
    private Long taskId;
    @Nullable
    private Boolean disableWebPagePreview;
    @Nullable
    private String parseMode;

    @Nullable
    public TelegramUser getSender() {
        return sender;
    }

    public void setSender(@Nullable TelegramUser sender) {
        this.sender = sender;
    }

    @Nullable
    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(@Nullable String receiver) {
        this.receiver = receiver;
    }

    @Nullable
    public String getText() {
        return text;
    }

    public void setText(@Nullable String text) {
        this.text = text;
    }

    @Nullable
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(@Nullable Long taskId) {
        this.taskId = taskId;
    }

    @Nullable
    public Boolean getDisableWebPagePreview() {
        return disableWebPagePreview;
    }

    public void setDisableWebPagePreview(@Nullable Boolean disableWebPagePreview) {
        this.disableWebPagePreview = disableWebPagePreview;
    }

    @Nullable
    public String getParseMode() {
        return parseMode;
    }

    public void setParseMode(@Nullable String parseMode) {
        this.parseMode = parseMode;
    }
}

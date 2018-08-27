package me.nizheg.telegram.bot.chgk.work;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import me.nizheg.telegram.bot.chgk.work.data.SendMessageData;

@Data
@Builder
public class SendMessageWork implements WorkDescription {

    private final long id;
    private final long chatId;
    @NonNull
    private final SendMessageData sendMessageData;

    public String getText() {
        return sendMessageData.getText();
    }

    public String getParseMode() {
        return sendMessageData.getParseMode();
    }

    public Boolean getDisableWebPagePreview() {
        return sendMessageData.getDisableWebPagePreview();
    }

}

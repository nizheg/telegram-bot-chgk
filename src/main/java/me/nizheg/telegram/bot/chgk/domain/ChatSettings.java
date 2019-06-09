package me.nizheg.telegram.bot.chgk.domain;

import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import me.nizheg.telegram.bot.service.PropertyService;
import me.nizheg.telegram.bot.starter.domain.ChatSetting;

/**
 * @author Nikolay Zhegalin
 */
@CommonsLog
public final class ChatSettings {

    private final ChatSetting<Boolean> isChatProtected;

    public ChatSettings(
            @NonNull PropertyService propertyService,
            long chatId) {
        isChatProtected = new ChatSetting<>(propertyService, chatId, "isChatProtected", Boolean.class);
    }

    public boolean isChatProtected() {
        return isChatProtected.getValue().orElse(Boolean.FALSE);
    }

    public void setChatProtected(boolean isSendBonusContent) {
        this.isChatProtected.setValue(isSendBonusContent);
    }

}

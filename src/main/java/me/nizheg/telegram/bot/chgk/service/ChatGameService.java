package me.nizheg.telegram.bot.chgk.service;

import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.dto.Chat;

public interface ChatGameService {

    void setTimer(long chatId, int timeoutSeconds);

    void clearTimer(long chatId);

    void stopChatGame(long chatId);

    ChatGame getGame(Chat chat);
}

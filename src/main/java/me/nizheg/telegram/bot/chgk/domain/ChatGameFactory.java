package me.nizheg.telegram.bot.chgk.domain;

import me.nizheg.telegram.bot.chgk.dto.Chat;

public interface ChatGameFactory {

    ChatGame createChatGame(Chat chat);

    AutoChatGame createAutoChatGame(Chat chat, int timeout);
}

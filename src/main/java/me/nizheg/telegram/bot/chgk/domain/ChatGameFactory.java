package me.nizheg.telegram.bot.chgk.domain;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.chgk.dto.Chat;

public interface ChatGameFactory {

    @Nonnull
    ChatGame createChatGame(Chat chat);

    @Nonnull
    AutoChatGame createAutoChatGame(Chat chat, int timeout);
}

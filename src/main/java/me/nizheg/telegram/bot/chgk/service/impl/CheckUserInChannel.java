package me.nizheg.telegram.bot.chgk.service.impl;

import org.ehcache.Cache;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.model.AtomicResponse;
import me.nizheg.telegram.bot.api.model.Chat;
import me.nizheg.telegram.bot.api.model.ChatMember;
import me.nizheg.telegram.bot.api.model.ChatMemberStatus;
import me.nizheg.telegram.bot.api.model.User;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.chgk.command.UserInChannel;
import me.nizheg.telegram.bot.chgk.command.exception.UserIsNotInChannelException;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.bot.service.impl.PreconditionChainStep;
import me.nizheg.telegram.bot.service.impl.PreconditionResult;
import me.nizheg.telegram.bot.starter.util.BotInfo;

import static me.nizheg.telegram.bot.api.model.ChatType.CHANNEL;

/**
 * @author Nikolay Zhegalin
 */
public class CheckUserInChannel extends PreconditionChainStep {

    private final Set<ChatMemberStatus> channelStatuses =
            EnumSet.of(ChatMemberStatus.ADMINISTRATOR, ChatMemberStatus.CREATOR, ChatMemberStatus.MEMBER);
    private final String channelName;
    private final Supplier<TelegramApiClient> telegramApiClientSupplier;
    private final Cache<Long, Boolean> cache;

    public CheckUserInChannel(
            @NonNull BotInfo botInfo,
            @NonNull String channelName,
            @NonNull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @NonNull Cache<Long, Boolean> cache) {
        this.cache = cache;
        if (!channelName.startsWith("@")) {
            channelName = "@" + channelName;
        }
        this.channelName = channelName;
        this.telegramApiClientSupplier = telegramApiClientSupplier;
        TelegramApiClient telegramApiClient = telegramApiClientSupplier.get();
        ChatId channelChatId = new ChatId(channelName);
        AtomicResponse<Chat> chatResponse = telegramApiClient.getChat(channelChatId).await();
        if (!chatResponse.isOk() || CHANNEL != chatResponse.getResult().getType()) {
            throw new IllegalArgumentException("Illegal channelName: " + channelName);
        }
        AtomicResponse<ChatMember> memberResponse = telegramApiClient.getChatMember(channelChatId,
                botInfo.getBotUser().getId()).await();
        if (!memberResponse.isOk() || ChatMemberStatus.ADMINISTRATOR != memberResponse.getResult().getStatus()) {
            throw new IllegalArgumentException("Bot should be administrator in channel " + channelName);
        }
    }

    @Override
    protected PreconditionResult doCheck(ChatCommand commandHandler, CommandContext context) throws CommandException {
        if (commandHandler != null && commandHandler.getClass().getAnnotation(UserInChannel.class) != null) {
            User user = context.getFrom();
            Boolean isUserAuthorizedCachedValue = cache.get(user.getId());
            boolean isUserAuthorized = true;
            if (isUserAuthorizedCachedValue == null || !isUserAuthorizedCachedValue) {
                AtomicResponse<ChatMember> chatMemberResponse = getTelegramApiClient()
                        .getChatMember(new ChatId(channelName), user.getId()).await();
                isUserAuthorized = chatMemberResponse.isOk()
                        && channelStatuses.contains(chatMemberResponse.getResult().getStatus());
                cache.put(user.getId(), isUserAuthorized);
            }
            if (!isUserAuthorized) {
                throw new UserIsNotInChannelException(user, channelName);
            }
        }
        return new PreconditionResult(true, "Check user in channel");
    }

    private TelegramApiClient getTelegramApiClient() {
        return telegramApiClientSupplier.get();
    }
}

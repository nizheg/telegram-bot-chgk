package me.nizheg.telegram.bot.chgk.service.impl;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.model.AtomicResponse;
import me.nizheg.telegram.bot.api.model.Chat;
import me.nizheg.telegram.bot.api.model.ChatMember;
import me.nizheg.telegram.bot.api.model.ChatMemberStatus;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.chgk.command.UserInChannel;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.service.impl.PreconditionChainStep;
import me.nizheg.telegram.bot.service.impl.PreconditionResult;

import static me.nizheg.telegram.bot.api.model.ChatType.CHANNEL;

/**
 * @author Nikolay Zhegalin
 */
public class CheckUserInChannel extends PreconditionChainStep {

    private final Set<ChatMemberStatus> channelStatuses =
            EnumSet.of(ChatMemberStatus.ADMINISTRATOR, ChatMemberStatus.CREATOR, ChatMemberStatus.MEMBER);
    private final String channelName;
    private final Supplier<TelegramApiClient> telegramApiClientSupplier;
    private final String errorMessage;

    public CheckUserInChannel(
            @NonNull String channelName,
            @NonNull Supplier<TelegramApiClient> telegramApiClientSupplier) {
        if (!channelName.startsWith("@")) {
            channelName = "@" + channelName;
        }
        this.channelName = channelName;
        this.telegramApiClientSupplier = telegramApiClientSupplier;
        AtomicResponse<Chat> chatResponse = telegramApiClientSupplier.get().getChat(new ChatId(channelName)).await();
        if (!chatResponse.getOk() || CHANNEL != chatResponse.getResult().getType()) {
            throw new IllegalArgumentException("Illegal channelName: " + channelName);
        }
        this.errorMessage = "Для того, чтобы пользоваться ботом, вы должны состоять в канале " + channelName;
    }

    @Override
    protected PreconditionResult doCheck(ChatCommand commandHandler, CommandContext context) {
        if (commandHandler != null && commandHandler.getClass().getAnnotation(UserInChannel.class) != null) {
            AtomicResponse<ChatMember> chatMemberResponse = telegramApiClientSupplier.get()
                    .getChatMember(new ChatId(channelName), context.getFrom().getId()).await();
            boolean isUserAuthorized =
                    chatMemberResponse.getOk() && channelStatuses.contains(chatMemberResponse.getResult().getStatus());
            if (!isUserAuthorized) {
                return new PreconditionResult(false, errorMessage);
            }
        }
        return new PreconditionResult(true, null);
    }
}

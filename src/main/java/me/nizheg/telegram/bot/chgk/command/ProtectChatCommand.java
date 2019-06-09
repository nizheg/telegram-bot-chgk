package me.nizheg.telegram.bot.chgk.command;

import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.model.ChatMemberStatus;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.starter.command.BooleanSettingCommand;
import me.nizheg.telegram.bot.starter.service.preconditions.Permission;

@Component
@Permission(chatMemberStatuses = {ChatMemberStatus.ADMINISTRATOR, ChatMemberStatus.CREATOR},
        failOnUnsatisfied = true,
        description = "Тольколь администраторы имеют право снимать ограничение")
public class ProtectChatCommand extends BooleanSettingCommand {

    public ProtectChatCommand(
            @NonNull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @NonNull ChatService chatService) {
        super("protect",
                "Управление только администраторами",
                telegramApiClientSupplier,
                chatId -> chatService.getSettings(chatId).isChatProtected(),
                (chatId, value) -> chatService.getSettings(chatId).setChatProtected(value));
    }

}

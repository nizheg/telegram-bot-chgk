package me.nizheg.telegram.bot.chgk.command;

import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.model.ChatMemberStatus;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.bot.starter.service.preconditions.Permission;

/**
 * @author Nikolay Zhegalin
 */
@UserInChannel
@Permission(chatMemberStatuses = {ChatMemberStatus.ADMINISTRATOR, ChatMemberStatus.CREATOR},
        failOnUnsatisfied = true,
        description = "Только администраторы имеют право получать новый вопрос")
@Component
public class ClearCurrentTaskAndSendNextCommand extends ChatCommand {

    private final ChatGameService chatGameService;

    public ClearCurrentTaskAndSendNextCommand(
            @NonNull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @NonNull ChatGameService chatGameService) {
        super(telegramApiClientSupplier);
        this.chatGameService = chatGameService;
    }

    @Override
    public int getPriority() {
        return 120;
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        chatGameService.getGame(new Chat(ctx.getChat())).clearCurrentTask();
        getCommandHolder().getCommandByName("next").execute(ctx);
    }

    @Override
    public String getCommandName() {
        return "clear_and_next";
    }

    @Override
    @Nullable
    public String getDescription() {
        return null;
    }
}

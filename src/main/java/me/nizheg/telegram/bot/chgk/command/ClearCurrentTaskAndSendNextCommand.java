package me.nizheg.telegram.bot.chgk.command;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;

/**
 * @author Nikolay Zhegalin
 */
public class ClearCurrentTaskAndSendNextCommand extends ChatCommand {

    private final ChatService chatService;

    public ClearCurrentTaskAndSendNextCommand(
            TelegramApiClient telegramApiClient,
            ChatService chatService) {
        super(telegramApiClient);
        this.chatService = chatService;
    }

    public ClearCurrentTaskAndSendNextCommand(
            Supplier<TelegramApiClient> telegramApiClientSupplier,
            ChatService chatService) {
        super(telegramApiClientSupplier);
        this.chatService = chatService;
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        chatService.getGame(new Chat(ctx.getChat())).clearCurrentTask();
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

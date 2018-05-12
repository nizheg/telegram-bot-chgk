package me.nizheg.telegram.bot.chgk.command;

import java.util.function.Supplier;

import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;

/**
 * @author Nikolay Zhegalin
 */
public class StopCommand extends ChatCommand {

    private final ChatService chatService;

    public StopCommand(TelegramApiClient telegramApiClient, ChatService chatService) {
        super(telegramApiClient);
        this.chatService = chatService;
    }

    public StopCommand(
            Supplier<TelegramApiClient> telegramApiClientSupplier,
            ChatService chatService) {
        super(telegramApiClientSupplier);
        this.chatService = chatService;
    }

    @Override
    public void execute(CommandContext ctx) {
        chatService.deactivateChat(ctx.getChatId());
        getTelegramApiClient().sendMessage(new Message("До новых встреч!", ctx.getChatId()));
    }

    @Override
    public String getCommandName() {
        return "stop";
    }

    @Override
    public String getDescription() {
        return "/stop - остановить бота";
    }
}

package me.nizheg.telegram.bot.chgk.command;

import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;

/**
 * @author Nikolay Zhegalin
 */
@Component
public class StopCommand extends ChatCommand {

    private final ChatService chatService;
    private final ChatGameService chatGameService;

    public StopCommand(
            @NonNull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @NonNull ChatService chatService,
            @NonNull ChatGameService chatGameService) {
        super(telegramApiClientSupplier);
        this.chatService = chatService;
        this.chatGameService = chatGameService;
    }

    @Override
    public int getPriority() {
        return 70;
    }

    @Override
    public void execute(CommandContext ctx) {
        chatGameService.stopChatGame(ctx.getChatId());
        chatService.deactivateChat(ctx.getChatId());
        getTelegramApiClient().sendMessage(Message.safeMessageBuilder()
                .text("До новых встреч!")
                .chatId(new ChatId(ctx.getChatId()))
                .build());
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

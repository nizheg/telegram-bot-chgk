package me.nizheg.telegram.bot.chgk.command;

import java.util.function.Supplier;

import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.util.NextTaskSender;
import me.nizheg.telegram.bot.command.CommandContext;

/**
 * @author Nikolay Zhegalin
 */
public class NextCommand extends ChatGameCommand {

    private final ChatService chatService;
    private final NextTaskSender nextTaskSender;

    public NextCommand(
            TelegramApiClient telegramApiClient,
            ChatService chatService,
            NextTaskSender nextTaskSender) {
        super(telegramApiClient);
        this.chatService = chatService;
        this.nextTaskSender = nextTaskSender;
    }

    public NextCommand(
            Supplier<TelegramApiClient> telegramApiClientSupplier,
            ChatService chatService, NextTaskSender nextTaskSender) {
        super(telegramApiClientSupplier);
        this.chatService = chatService;
        this.nextTaskSender = nextTaskSender;
    }

    @Override
    protected ChatService getChatService() {
        return chatService;
    }

    @Override
    protected void executeChatGame(CommandContext ctx, ChatGame chatGame) {
        nextTaskSender.sendNextTask(chatGame);
    }

    @Override
    public String getCommandName() {
        return "next";
    }

    @Override
    public String getDescription() {
        return "/next - получить следующий вопрос";
    }
}

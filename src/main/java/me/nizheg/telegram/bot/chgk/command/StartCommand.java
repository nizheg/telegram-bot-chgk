package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.Validate;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.service.CommandsHolder;

/**
 * @author Nikolay Zhegalin
 */
public class StartCommand extends ChatCommand {

    private final ChatService chatService;

    public StartCommand(
            @Nonnull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @Nonnull ChatService chatService) {
        super(telegramApiClientSupplier);
        Validate.notNull(chatService, "chatService should be defined");
        this.chatService = chatService;
    }

    @Override
    public void execute(CommandContext ctx) {
        Long chatId = ctx.getChatId();
        if (!chatService.isChatActive(chatId)) {
            chatService.activateChat(chatId);
            getTelegramApiClient().sendMessage(new Message(
                    "Привет. Узнать, что я умею, можно с помощью команды /help.\nЧтобы получить вопрос, нажмите /next",
                    chatId));
        } else {
            getTelegramApiClient().sendMessage(
                    new Message("Чтобы повторить вопрос, воспользуйтесь /repeat. Чтобы получить следующее - /next.",
                            chatId));
        }
    }

    @Override
    public String getCommandName() {
        return CommandsHolder.COMMAND_NAME_START;
    }

    @Override
    public String getDescription() {
        return "/start - запустить бота";
    }
}

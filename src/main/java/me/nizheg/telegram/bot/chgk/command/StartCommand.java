package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.service.CommandsHolder;

/**
 * @author Nikolay Zhegalin
 */
@Component
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
    public int getPriority() {
        return 10;
    }

    @Override
    public void execute(CommandContext ctx) {
        Long chatId = ctx.getChatId();
        if (!chatService.isChatActive(chatId)) {
            chatService.activateChat(chatId);
            getTelegramApiClient().sendMessage(Message.safeMessageBuilder()
                    .text("Привет. Узнать, что я умею, можно с помощью команды /help.\nЧтобы получить вопрос, нажмите /next")
                    .chatId(new ChatId(ctx.getChatId()))
                    .build());
        } else {
            getTelegramApiClient().sendMessage(Message.safeMessageBuilder()
                               .text("Чтобы повторить вопрос, воспользуйтесь /repeat. Чтобы получить следующее - /next.")
                               .chatId(new ChatId(ctx.getChatId()))
                               .build());
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

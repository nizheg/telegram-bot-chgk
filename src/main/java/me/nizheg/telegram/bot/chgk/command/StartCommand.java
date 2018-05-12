package me.nizheg.telegram.bot.chgk.command;

import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.bot.service.CommandsHolder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Supplier;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class StartCommand extends ChatCommand {

    @Autowired
    private ChatService chatService;

    public StartCommand(TelegramApiClient telegramApiClient) {
        super(telegramApiClient);
    }

    public StartCommand(Supplier<TelegramApiClient> telegramApiClientSupplier) {
        super(telegramApiClientSupplier);
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        Long chatId = ctx.getChatId();
        if (!chatService.isChatActive(chatId)) {
            chatService.activateChat(chatId);
            getTelegramApiClient().sendMessage(new Message("Привет. Узнать, что я умею, можно с помощью команды /help.\nЧтобы получить вопрос, нажмите /next",
                    chatId));
        } else {
            getTelegramApiClient().sendMessage(new Message("Чтобы повторить вопрос, воспользуйтесь /repeat. Чтобы получить следующее - /next.", chatId));
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

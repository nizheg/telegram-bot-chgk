package me.nizheg.telegram.bot.chgk.command;

import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.service.CommandsHolder;
import me.nizheg.telegram.bot.service.command.ChatCommand;
import me.nizheg.telegram.bot.service.command.CommandContext;
import me.nizheg.telegram.bot.service.command.CommandException;
import me.nizheg.telegram.service.TelegramApiClient;
import me.nizheg.telegram.service.param.Message;

import org.springframework.beans.factory.annotation.Autowired;

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

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        Long chatId = ctx.getChatId();
        if (!chatService.isChatActive(chatId)) {
            chatService.activateChat(chatId);
            telegramApiClient.sendMessage(new Message("Привет. Узнать, что я умею, можно с помощью команды /help.\nЧтобы получить вопрос, нажмите /next",
                    chatId));
        } else {
            telegramApiClient.sendMessage(new Message("Чтобы повторить вопрос, воспользуйтесь /repeat. Чтобы получить следующее - /next.", chatId));
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

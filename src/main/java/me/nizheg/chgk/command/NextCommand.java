package me.nizheg.chgk.command;

import me.nizheg.chgk.domain.ChatGame;
import me.nizheg.chgk.service.ChatService;
import me.nizheg.chgk.util.NextTaskSender;
import me.nizheg.telegram.bot.service.command.CommandContext;
import me.nizheg.telegram.bot.service.command.CommandException;
import me.nizheg.telegram.service.TelegramApiClient;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class NextCommand extends ChatGameCommand {

    @Autowired
    private ChatService chatService;
    @Autowired
    private NextTaskSender nextTaskSender;

    public NextCommand(TelegramApiClient telegramApiClient) {
        super(telegramApiClient);
    }

    @Override
    protected ChatService getChatService() {
        return chatService;
    }

    @Override
    protected void executeChatGame(CommandContext ctx, ChatGame chatGame) throws CommandException {
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

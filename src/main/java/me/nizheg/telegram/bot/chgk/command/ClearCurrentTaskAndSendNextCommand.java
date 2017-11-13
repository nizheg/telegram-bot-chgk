package me.nizheg.telegram.bot.chgk.command;

import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.service.TelegramApiClient;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Nikolay Zhegalin
 */
public class ClearCurrentTaskAndSendNextCommand extends ChatCommand {

    @Autowired
    private ChatService chatService;

    public ClearCurrentTaskAndSendNextCommand(TelegramApiClient telegramApiClient) {
        super(telegramApiClient);
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
    public String getDescription() {
        return null;
    }
}

package me.nizheg.chgk.command;

import me.nizheg.chgk.service.ChatService;
import me.nizheg.telegram.bot.service.command.ChatCommand;
import me.nizheg.telegram.bot.service.command.CommandContext;
import me.nizheg.telegram.service.TelegramApiClient;

import me.nizheg.telegram.service.param.Message;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class StopCommand extends ChatCommand {
    @Autowired
    private ChatService chatService;

    public StopCommand(TelegramApiClient telegramApiClient) {
        super(telegramApiClient);
    }

    @Override
    public void execute(CommandContext ctx) {
        chatService.deactivateChat(ctx.getChatId());
        telegramApiClient.sendMessage(new Message("До новых встреч!", ctx.getChatId()));
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

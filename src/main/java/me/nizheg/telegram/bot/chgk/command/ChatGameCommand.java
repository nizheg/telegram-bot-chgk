package me.nizheg.telegram.bot.chgk.command;

import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.service.command.ChatCommand;
import me.nizheg.telegram.bot.service.command.CommandContext;
import me.nizheg.telegram.bot.service.command.CommandException;
import me.nizheg.telegram.model.ParseMode;
import me.nizheg.telegram.service.TelegramApiClient;
import me.nizheg.telegram.service.param.Message;

/**
 * @author Nikolay Zhegalin
 */
public abstract class ChatGameCommand extends ChatCommand {

    public ChatGameCommand(TelegramApiClient telegramApiClient) {
        super(telegramApiClient);
    }

    protected abstract ChatService getChatService();

    protected abstract void executeChatGame(CommandContext ctx, ChatGame chatGame) throws CommandException;

    @Override
    public final void execute(CommandContext ctx) throws CommandException {
        Long chatId = ctx.getChatId();
        ChatService chatService = getChatService();
        boolean isChatActive = chatService.isChatActive(chatId);
        ChatGame chatGame = null;
        if (isChatActive) {
            chatGame = chatService.getGame(new Chat(ctx.getChat()));
        }
        if (chatGame == null) {
            throw new CommandException(new Message("<i>Необходимо активировать бота с помощью команды</i> /start", chatId, ParseMode.HTML));
        }
        executeChatGame(ctx, chatGame);
    }
}

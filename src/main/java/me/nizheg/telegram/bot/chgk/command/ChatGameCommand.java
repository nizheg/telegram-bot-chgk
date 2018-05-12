package me.nizheg.telegram.bot.chgk.command;

import java.util.function.Supplier;

import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;

/**
 * @author Nikolay Zhegalin
 */
public abstract class ChatGameCommand extends ChatCommand {

    public ChatGameCommand(TelegramApiClient telegramApiClient) {
        super(telegramApiClient);
    }

    public ChatGameCommand(Supplier<TelegramApiClient> telegramApiClientSupplier) {
        super(telegramApiClientSupplier);
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
            throw new CommandException(
                    new Message("<i>Необходимо активировать бота с помощью команды</i> /start", chatId,
                            ParseMode.HTML));
        }
        executeChatGame(ctx, chatGame);
    }
}

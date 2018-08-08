package me.nizheg.telegram.bot.chgk.command;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.chgk.command.exception.BotIsNotStartedException;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;

/**
 * @author Nikolay Zhegalin
 */
public abstract class ChatGameCommand extends ChatCommand {

    public ChatGameCommand(@Nonnull TelegramApiClient telegramApiClient) {
        super(telegramApiClient);
    }

    public ChatGameCommand(@Nonnull Supplier<TelegramApiClient> telegramApiClientSupplier) {
        super(telegramApiClientSupplier);
    }

    protected abstract ChatService getChatService();

    protected abstract ChatGameService getChatGameService();

    protected abstract void executeChatGame(CommandContext ctx, ChatGame chatGame) throws CommandException;

    @Override
    public final void execute(CommandContext ctx) throws CommandException {
        Long chatId = ctx.getChatId();
        boolean isChatActive = getChatService().isChatActive(chatId);
        ChatGame chatGame = null;
        if (isChatActive) {
            chatGame = getChatGameService().getGame(new Chat(ctx.getChat()));
        }
        if (chatGame == null) {
            throw new BotIsNotStartedException();
        }
        executeChatGame(ctx, chatGame);
    }
}

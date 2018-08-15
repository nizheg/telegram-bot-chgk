package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.Validate;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;

/**
 * @author Nikolay Zhegalin
 */
@UserInChannel
public class ClearCurrentTaskAndSendNextCommand extends ChatCommand {

    private final ChatGameService chatGameService;

    public ClearCurrentTaskAndSendNextCommand(
            @Nonnull TelegramApiClient telegramApiClient,
            @Nonnull ChatGameService chatGameService) {
        super(telegramApiClient);
        Validate.notNull(chatGameService, "chatGameService should be defined");
        this.chatGameService = chatGameService;
    }

    public ClearCurrentTaskAndSendNextCommand(
            @Nonnull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @Nonnull ChatGameService chatGameService) {
        super(telegramApiClientSupplier);
        Validate.notNull(chatGameService, "chatGameService should be defined");
        this.chatGameService = chatGameService;
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        chatGameService.getGame(new Chat(ctx.getChat())).clearCurrentTask();
        getCommandHolder().getCommandByName("next").execute(ctx);
    }

    @Override
    public String getCommandName() {
        return "clear_and_next";
    }

    @Override
    @Nullable
    public String getDescription() {
        return null;
    }
}

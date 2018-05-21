package me.nizheg.telegram.bot.chgk.command;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.dto.BroadcastStatus;
import me.nizheg.telegram.bot.chgk.service.MessageService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;

public class BroadcastCommand extends ChatCommand {

    private final MessageService messageService;

    public BroadcastCommand(
            @Nonnull Supplier<TelegramApiClient> telegramApiClientSupplier,
            MessageService messageService) {
        super(telegramApiClientSupplier);
        this.messageService = messageService;
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        BroadcastStatus broadcastStatus = messageService.forwardToAll();
        switch (broadcastStatus.getStatus()) {
            case REJECTED:
                getTelegramApiClient().sendMessage(new Message(broadcastStatus.getErrorMessage(), ctx.getFrom().getId
                        ()));
            default:
                getTelegramApiClient().sendMessage(
                        new Message(broadcastStatus.getSendingMessage(), ctx.getFrom().getId()));
        }
    }

    @Override
    public String getCommandName() {
        return "broadcast_forward";
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }
}

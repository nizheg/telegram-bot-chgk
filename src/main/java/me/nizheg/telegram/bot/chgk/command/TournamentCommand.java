package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.Validate;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.TelegramApiException;
import me.nizheg.telegram.bot.api.service.param.EditedMessage;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.util.TourList;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;

/**
 * @author Nikolay Zhegalin
 */
public class TournamentCommand extends ChatCommand {

    private static final String COMMAND_NAME = "tournament";

    private final ChatService chatService;
    private final TourList tourList;

    public TournamentCommand(
            @Nonnull TelegramApiClient telegramApiClient,
            @Nonnull ChatService chatService,
            TourList tourList) {
        super(telegramApiClient);
        Validate.notNull(chatService, "chatService should be defined");
        Validate.notNull(tourList, "tourList should be defined");
        this.chatService = chatService;
        this.tourList = tourList;
    }

    public TournamentCommand(
            @Nonnull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @Nonnull ChatService chatService, TourList tourList) {
        super(telegramApiClientSupplier);
        Validate.notNull(chatService, "chatService should be defined");
        Validate.notNull(tourList, "tourList should be defined");
        this.chatService = chatService;
        this.tourList = tourList;
    }

    @Override
    public void execute(CommandContext ctx) {
        if (!chatService.isChatActive(ctx.getChatId())) {
            return;
        }
        int page;
        try {
            page = Integer.valueOf(ctx.getText());
        } catch (NumberFormatException ex) {
            page = 0;
        }
        Message tournamentsList = tourList.getTournamentsListOfChat(ctx.getChatId(), page);
        if (ctx.getReplyToBotMessage() != null) {
            try {
                getTelegramApiClient().editMessageText(
                        new EditedMessage(tournamentsList, ctx.getReplyToBotMessage().getMessageId()));
            } catch (TelegramApiException ex) {
                logger.warn("Unable to edit message of tournaments", ex);
            }
        } else {
            getTelegramApiClient().sendMessage(tournamentsList);
        }
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return "/tournament - выбрать турнир для прохождения; чтобы потом вернуть произвольный порядок выдачи, выберите другую категорию";
    }
}

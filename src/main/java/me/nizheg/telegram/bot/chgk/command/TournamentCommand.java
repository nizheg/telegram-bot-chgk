package me.nizheg.telegram.bot.chgk.command;

import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.util.TourList;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.service.TelegramApiClient;
import me.nizheg.telegram.service.TelegramApiException;
import me.nizheg.telegram.service.param.EditedMessage;
import me.nizheg.telegram.service.param.Message;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class TournamentCommand extends ChatCommand {

    private static final String COMMAND_NAME = "tournament";

    @Autowired
    private ChatService chatService;
    @Autowired
    private TourList tourList;

    public TournamentCommand(TelegramApiClient telegramApiClient) {
        super(telegramApiClient);
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        if (!chatService.isChatActive(ctx.getChatId())) {
            return;
        }
        int page = 0;
        try {
            page = Integer.valueOf(ctx.getText());
        } catch (NumberFormatException ex) {
        }
        Message tournamentsList = tourList.getTournamentsListOfChat(ctx.getChatId(), page);
        if (ctx.getReplyToBotMessage() != null) {
            try {
                telegramApiClient.editMessageText(new EditedMessage(tournamentsList, ctx.getReplyToBotMessage().getMessageId()));
            } catch (TelegramApiException ex) {
                logger.warn("Unable to edit message of tournaments", ex);
            }
        } else {
            telegramApiClient.sendMessage(tournamentsList);
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

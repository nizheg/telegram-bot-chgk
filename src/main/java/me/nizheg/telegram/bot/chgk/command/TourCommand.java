package me.nizheg.telegram.bot.chgk.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.dto.composite.Tournament;
import me.nizheg.telegram.bot.chgk.exception.IllegalIdException;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.util.TourList;
import me.nizheg.telegram.bot.service.command.CommandContext;
import me.nizheg.telegram.bot.service.command.CommandException;
import me.nizheg.telegram.model.ParseMode;
import me.nizheg.telegram.model.ReplyMarkup;
import me.nizheg.telegram.service.TelegramApiClient;
import me.nizheg.telegram.service.param.Message;
import me.nizheg.telegram.util.TelegramApiUtil;
import me.nizheg.telegram.util.TelegramHtmlUtil;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Nikolay Zhegalin
 */
public class TourCommand extends ChatGameCommand {

    private static final String COMMAND_FORMAT = "tour_?([0-9]+)?";
    private static final Pattern COMMAND_PATTERN = Pattern.compile(COMMAND_FORMAT);
    @Autowired
    private ChatService chatService;
    @Autowired
    private TourList tourList;

    public TourCommand(TelegramApiClient telegramApiClient) {
        super(telegramApiClient);
    }

    @Override
    protected ChatService getChatService() {
        return chatService;
    }

    @Override
    protected void executeChatGame(CommandContext ctx, ChatGame chatGame) throws CommandException {
        Long chatId = ctx.getChatId();
        String command = ctx.getCommand();
        Matcher commandMatcher = COMMAND_PATTERN.matcher(command);
        long tourId = 0;
        if (commandMatcher.matches()) {
            String parsedTourId = commandMatcher.group(1);
            if (parsedTourId != null) {
                tourId = Long.valueOf(parsedTourId);
            }
        } else {
            return;
        }

        String toursOfTourGroup = tourList.getToursListOfTourGroup(tourId);
        if (toursOfTourGroup != null) {
            telegramApiClient.sendMessage(new Message(toursOfTourGroup, chatId));
            return;
        }

        try {
            Tournament tournament = chatGame.setTournament(tourId);
            boolean isCurrentTaskFromTournament = chatGame.isCurrentTaskFromTournament();
            ReplyMarkup buttonMarkup;
            if (isCurrentTaskFromTournament) {
                buttonMarkup = TelegramApiUtil.createInlineButtonMarkup("Начать заново", "clear_and_next", "Продолжить", "next");
            } else {
                buttonMarkup = TelegramApiUtil.createInlineButtonMarkup("Начать", "next");
            }
            telegramApiClient.sendMessage(new Message("Выбран турнир <b>" + TelegramHtmlUtil.escape(tournament.getTitle()) + "</b>", chatId, ParseMode.HTML,
                    true, null, buttonMarkup));
        } catch (IllegalIdException e) {
            logger.error("Illegal id of tournament for chat " + chatId, e);
            throw new CommandException(new Message("<i>Неверный идентификатор турнира</i>", chatId, ParseMode.HTML));
        }
    }

    @Override
    public String getCommandName() {
        return COMMAND_FORMAT;
    }

    @Override
    public String getDescription() {
        return null;
    }
}

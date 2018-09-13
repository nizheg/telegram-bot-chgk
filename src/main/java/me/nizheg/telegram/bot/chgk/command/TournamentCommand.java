package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.EditedMessage;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.util.TourList;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;

/**
 * @author Nikolay Zhegalin
 */
@ChatActive
public class TournamentCommand extends ChatCommand {

    private static final String COMMAND_NAME = "tournament";
    private static final Pattern COMMAND_PATTERN = Pattern.compile("(?:page(?<page>[0-9]+))?(?<query>.*)");

    private final TourList tourList;

    public TournamentCommand(
            @NonNull Supplier<TelegramApiClient> telegramApiClientSupplier, @NonNull TourList tourList) {
        super(telegramApiClientSupplier);
        this.tourList = tourList;
    }

    @Override
    public void execute(CommandContext ctx) {
        Matcher matcher = COMMAND_PATTERN.matcher(ctx.getText());
        Optional<String> queryParam = Optional.empty();
        Optional<Integer> pageParam = Optional.empty();
        if (matcher.matches()) {
            queryParam = Optional.ofNullable(matcher.group("query")).filter(StringUtils::isNotBlank);
            pageParam = Optional.ofNullable(matcher.group("page")).map(Integer::valueOf);
        }
        int page = pageParam.orElse(0);
        if (queryParam.isPresent()) {
            Message tournamentsList = tourList.getFilteredTournamentsListOfChat(ctx.getChatId(), queryParam.get());
            getTelegramApiClient().sendMessage(tournamentsList);
        } else {
            Message tournamentsList = tourList.getTournamentsListOfChat(ctx.getChatId(), page);
            if (ctx.getReplyToBotMessage() != null) {
                getTelegramApiClient().editMessageText(
                        new EditedMessage(tournamentsList, ctx.getReplyToBotMessage().getMessageId()));
            } else {
                getTelegramApiClient().sendMessage(tournamentsList);
            }
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

package me.nizheg.telegram.bot.chgk.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.AnswerCallbackRequest;
import me.nizheg.telegram.bot.chgk.dto.TaskRating;
import me.nizheg.telegram.bot.chgk.service.TaskRatingService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.util.Emoji;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class RatingCommand extends ChatCommand {

    private static final String PARAMS_FORMAT = "([+-][0-9]+) ([0-9]+)";
    private static final Pattern PARAMS_PATTERN = Pattern.compile(PARAMS_FORMAT);
    private static final String ATTRIBUTE_TASK_ID = "RATING_TASK_ID";

    @Autowired
    private TaskRatingService taskRatingService;

    public RatingCommand(TelegramApiClient telegramApiClient) {
        super(telegramApiClient);
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        String params = StringUtils.defaultString(ctx.getText());
        Matcher commandMatcher = PARAMS_PATTERN.matcher(params);
        int newRatingValue = 0;
        Long taskId = null;
        if (commandMatcher.matches()) {
            try {
                newRatingValue = Integer.valueOf(commandMatcher.group(1));
                taskId = Long.valueOf(commandMatcher.group(2));
            } catch (NumberFormatException ex) {
                // ignore
            }
        }
        if (taskId == null) {
            return;
        }
        ctx.setAttribute(ATTRIBUTE_TASK_ID, taskId);
        Long telegramUserId = ctx.getFrom().getId();
        if (newRatingValue > 0) {
            taskRatingService.upTaskRatingByUser(taskId, telegramUserId);
        } else if (newRatingValue < 0) {
            taskRatingService.downTaskRatingByUser(taskId, telegramUserId);
        }

    }

    @Override
    public void sendCallbackResponse(CommandContext ctx) {
        AnswerCallbackRequest answerCallbackRequest = new AnswerCallbackRequest();
        answerCallbackRequest.setCallBackQueryId(ctx.getCallbackQueryId());
        Long taskId = (Long) ctx.getAttribute(ATTRIBUTE_TASK_ID);
        if (taskId != null) {
            TaskRating taskRating = taskRatingService.getTaskRatingByTaskId(taskId);
            String currentRating = Emoji.THUMBS_UP_SIGN + " " + taskRating.getLikesCount() + " " + Emoji.THUMBS_DOWN_SIGN + " " + taskRating.getDislikesCount();
            answerCallbackRequest.setText("Рейтинг вопроса: " + currentRating);
        }
        telegramApiClient.answerCallbackQuery(answerCallbackRequest);
    }

    @Override
    public String getCommandName() {
        return "rating";
    }

    @Override
    public String getDescription() {
        return null;
    }
}

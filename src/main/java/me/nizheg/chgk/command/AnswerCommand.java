package me.nizheg.chgk.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.nizheg.chgk.domain.ChatGame;
import me.nizheg.chgk.domain.HintResult;
import me.nizheg.chgk.dto.Chat;
import me.nizheg.chgk.exception.NoTaskException;
import me.nizheg.chgk.service.ChatService;
import me.nizheg.chgk.util.AnswerSender;
import me.nizheg.chgk.util.RatingHelper;
import me.nizheg.telegram.bot.service.command.CommandContext;
import me.nizheg.telegram.bot.service.command.CommandException;
import me.nizheg.telegram.model.InlineKeyboardButton;
import me.nizheg.telegram.model.InlineKeyboardMarkup;
import me.nizheg.telegram.service.TelegramApiClient;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class AnswerCommand extends ChatGameCommand {

    @Autowired
    private ChatService chatService;
    @Autowired
    private AnswerSender answerSender;
    @Autowired
    private RatingHelper ratingHelper;

    public AnswerCommand(TelegramApiClient telegramApiClient) {
        super(telegramApiClient);
    }

    @Override
    protected ChatService getChatService() {
        return chatService;
    }

    @Override
    protected void executeChatGame(CommandContext ctx, ChatGame chatGame) throws CommandException {
        Long chatId = ctx.getChatId();
        Long taskId = parseTaskId(ctx);
        HintResult hintForTask = chatGame.getHintForTask(new Chat(ctx.getChat()), taskId);
        if (hintForTask.getTask() != null) {
            InlineKeyboardMarkup replyMarkup = null;
            if (hintForTask.isTaskCurrent()) {
                replyMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> buttonGroup = new ArrayList<List<InlineKeyboardButton>>();
                buttonGroup.add(ratingHelper.createRatingButtons(hintForTask.getTask().getId()));
                InlineKeyboardButton nextButton = new InlineKeyboardButton();
                nextButton.setText("Дальше");
                nextButton.setCallbackData("next");
                buttonGroup.add(Collections.singletonList(nextButton));
                replyMarkup.setInlineKeyboard(buttonGroup);
            }
            StringBuilder messageBuilder = new StringBuilder("<b>Ответ:</b>\n");
            answerSender.sendAnswerOfTask(messageBuilder, hintForTask.getTask(), chatId, replyMarkup);
        } else {
            throw new NoTaskException(ctx.getChatId());
        }
    }

    private Long parseTaskId(CommandContext ctx) {
        Long taskId = null;
        if (ctx.getText() != null) {
            try {
                taskId = Long.valueOf(ctx.getText());
            } catch (NumberFormatException ex) {
                // ignore
            }
        }
        return taskId;
    }

    @Override
    public String getCommandName() {
        return "answer";
    }

    @Override
    public String getDescription() {
        return "/answer - получить ответ на текущий вопрос";
    }
}

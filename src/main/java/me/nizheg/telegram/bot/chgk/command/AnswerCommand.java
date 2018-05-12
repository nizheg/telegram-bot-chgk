package me.nizheg.telegram.bot.chgk.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import me.nizheg.telegram.bot.api.model.InlineKeyboardButton;
import me.nizheg.telegram.bot.api.model.InlineKeyboardMarkup;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.domain.HintResult;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.exception.NoTaskException;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.util.AnswerSender;
import me.nizheg.telegram.bot.chgk.util.RatingHelper;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;

/**

 *
 * @author Nikolay Zhegalin
 */
public class AnswerCommand extends ChatGameCommand {

    private final ChatService chatService;
    private final AnswerSender answerSender;
    private final RatingHelper ratingHelper;

    public AnswerCommand(
            TelegramApiClient telegramApiClient,
            ChatService chatService,
            AnswerSender answerSender,
            RatingHelper ratingHelper) {
        super(telegramApiClient);
        this.chatService = chatService;
        this.answerSender = answerSender;
        this.ratingHelper = ratingHelper;
    }

    public AnswerCommand(
            Supplier<TelegramApiClient> telegramApiClientSupplier,
            ChatService chatService,
            AnswerSender answerSender,
            RatingHelper ratingHelper) {
        super(telegramApiClientSupplier);
        this.chatService = chatService;
        this.answerSender = answerSender;
        this.ratingHelper = ratingHelper;
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

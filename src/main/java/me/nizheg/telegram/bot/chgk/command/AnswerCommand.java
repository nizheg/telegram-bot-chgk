package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.api.model.InlineKeyboardButton;
import me.nizheg.telegram.bot.api.model.InlineKeyboardMarkup;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.domain.HintResult;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.exception.NoTaskException;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.util.AnswerSender;
import me.nizheg.telegram.bot.chgk.util.RatingHelper;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;

/**
 * @author Nikolay Zhegalin
 */
public class AnswerCommand extends ChatGameCommand {

    private final ChatService chatService;
    private final ChatGameService chatGameService;
    private final AnswerSender answerSender;
    private final RatingHelper ratingHelper;

    public AnswerCommand(
            @Nonnull TelegramApiClient telegramApiClient,
            @Nonnull ChatService chatService,
            @Nonnull ChatGameService chatGameService,
            @Nonnull AnswerSender answerSender,
            @Nonnull RatingHelper ratingHelper) {
        super(telegramApiClient);
        Validate.notNull(chatService, "chatService should be defined");
        Validate.notNull(chatGameService, "chatGameService should be defined");
        Validate.notNull(answerSender, "answerSender should be defined");
        Validate.notNull(ratingHelper, "ratingHelper should be defined");
        this.chatService = chatService;
        this.chatGameService = chatGameService;
        this.answerSender = answerSender;
        this.ratingHelper = ratingHelper;
    }

    public AnswerCommand(
            @Nonnull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @Nonnull ChatService chatService,
            @Nonnull ChatGameService chatGameService,
            @Nonnull AnswerSender answerSender,
            @Nonnull RatingHelper ratingHelper) {
        super(telegramApiClientSupplier);
        Validate.notNull(chatService, "chatService should be defined");
        Validate.notNull(chatGameService, "chatGameService should be defined");
        Validate.notNull(answerSender, "answerSender should be defined");
        Validate.notNull(ratingHelper, "ratingHelper should be defined");
        this.chatService = chatService;
        this.chatGameService = chatGameService;
        this.answerSender = answerSender;
        this.ratingHelper = ratingHelper;
    }

    @Override
    protected ChatService getChatService() {
        return chatService;
    }

    @Override
    protected ChatGameService getChatGameService() {
        return chatGameService;
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
                List<List<InlineKeyboardButton>> buttonGroup = new ArrayList<>();
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

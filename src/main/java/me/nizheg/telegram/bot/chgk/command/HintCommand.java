package me.nizheg.telegram.bot.chgk.command;

import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.model.AbstractCallback;
import me.nizheg.telegram.bot.api.model.AtomicResponse;
import me.nizheg.telegram.bot.api.model.ErrorResponse;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.AnswerCallbackRequest;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.command.exception.NoTaskException;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.domain.HintResult;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.exception.GameException;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.util.AnswerSender;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;

/**
 * @author Nikolay Zhegalin
 */
public class HintCommand extends ChatGameCommand {

    private final ChatService chatService;
    private final ChatGameService chatGameService;
    private final AnswerSender answerSender;

    public HintCommand(
            @NonNull TelegramApiClient telegramApiClient,
            @NonNull ChatService chatService,
            @NonNull ChatGameService chatGameService,
            @NonNull AnswerSender answerSender) {
        super(telegramApiClient);
        this.chatService = chatService;
        this.chatGameService = chatGameService;
        this.answerSender = answerSender;
    }

    public HintCommand(
            @NonNull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @NonNull ChatService chatService,
            @NonNull ChatGameService chatGameService,
            @NonNull AnswerSender answerSender) {
        super(telegramApiClientSupplier);
        this.chatService = chatService;
        this.chatGameService = chatGameService;
        this.answerSender = answerSender;
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
        Long taskId = parseTaskId(ctx);
        try {
            HintResult hintForTask = chatGame.getHintForTask(new Chat(ctx.getFrom()), taskId);
            Task hintForTaskTask = hintForTask.getTask().orElseThrow(NoTaskException::new);
            sendAnswerToUser(ctx, hintForTaskTask);
        } catch (GameException e) {
            throw new CommandException(e.getMessage(), e);
        }
    }

    private void sendAnswerToUser(CommandContext ctx, Task currentTask) {
        TelegramApiClient telegramApiClient = getTelegramApiClient();
        answerSender.sendAnswer(currentTask, false, ctx.getFrom().getId(), new Callback(ctx, telegramApiClient));
    }

    @Override
    public void sendCallbackResponse(CommandContext ctx) {
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
        return "hint";
    }

    @Override
    public String getDescription() {
        return "/hint - получить ответ на текущий вопрос в личку";
    }

    private class Callback extends AbstractCallback<AtomicResponse<me.nizheg.telegram.bot.api.model.Message>> {

        private static final String BOT_BLOCKED_ERROR = "Не удалось отправить подсказку. Проверьте, что бот знает вас "
                + "(необходимо выполнить /start в личке с ним)";
        private static final String NEW_USER_ERROR = "Не удалось отправить подсказку. Активируйте бота с помощью "
                + "команды /start в личке с ним";
        private static final String UNEXPECTED_ERROR = "По какой-то причине не удалось отправить подсказку. Свяжитесь с администратором";
        private final CommandContext ctx;
        private final TelegramApiClient telegramApiClient;

        public Callback(CommandContext ctx, TelegramApiClient telegramApiClient) {
            this.ctx = ctx;
            this.telegramApiClient = telegramApiClient;
        }

        @Override
        public void onFailure(
                ErrorResponse errorResponse, HttpStatus httpStatus) {
            if (ctx.getCallbackQueryId() != null) {
                AnswerCallbackRequest answerCallbackRequest = new AnswerCallbackRequest();
                answerCallbackRequest.setCallBackQueryId(ctx.getCallbackQueryId());
                answerCallbackRequest.setShowAlert(true);
                if (isBotBlocked(errorResponse, httpStatus)) {
                    answerCallbackRequest.setText(BOT_BLOCKED_ERROR);
                } else if (isNewUser(errorResponse, httpStatus)) {
                    answerCallbackRequest.setText(NEW_USER_ERROR);
                } else {
                    answerCallbackRequest.setText(UNEXPECTED_ERROR);
                }
                telegramApiClient.answerCallbackQuery(answerCallbackRequest);

            } else {
                if (isBotBlocked(errorResponse, httpStatus)) {
                    telegramApiClient.sendMessage(new Message("<i>" + BOT_BLOCKED_ERROR + "</i>",
                            ctx.getChatId(), ParseMode.HTML));
                } else if (isNewUser(errorResponse, httpStatus)) {
                    telegramApiClient.sendMessage(
                            new Message("<i>" + NEW_USER_ERROR + "</i>", ctx.getChatId(), ParseMode.HTML));
                } else {
                    telegramApiClient.sendMessage(
                            new Message("<i>" + UNEXPECTED_ERROR + "</i>", ctx.getChatId(), ParseMode.HTML));
                }
            }
        }

        @Override
        public void onSuccessResult(AtomicResponse<me.nizheg.telegram.bot.api.model.Message> result) {
            if (ctx.getCallbackQueryId() != null) {
                AnswerCallbackRequest answerCallbackRequest = new AnswerCallbackRequest();
                answerCallbackRequest.setCallBackQueryId(ctx.getCallbackQueryId());
                telegramApiClient.answerCallbackQuery(answerCallbackRequest);
            }
        }
    }
}

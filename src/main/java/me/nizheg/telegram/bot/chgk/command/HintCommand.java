package me.nizheg.telegram.bot.chgk.command;

import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.model.AtomicResponse;
import me.nizheg.telegram.bot.api.model.Callback;
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
        answerSender.sendAnswer(currentTask, false, ctx.getFrom().getId(),
                new Callback<AtomicResponse<me.nizheg.telegram.bot.api.model.Message>>() {
                    @Override
                    public void onFailure(
                            ErrorResponse errorResponse, HttpStatus httpStatus) {
                        if (ctx.getCallbackQueryId() != null) {
                            AnswerCallbackRequest answerCallbackRequest = new AnswerCallbackRequest();
                            answerCallbackRequest.setCallBackQueryId(ctx.getCallbackQueryId());
                            answerCallbackRequest.setShowAlert(true);
                            answerCallbackRequest.setText("Не удалось отправить подсказку. Проверьте, что бот знает вас "
                                    + "(необходимо выполнить /start в личке с ним) и он не заблокирован.");
                            telegramApiClient.answerCallbackQuery(answerCallbackRequest);

                        } else {
                            telegramApiClient
                                    .sendMessage(new Message(
                                            "<i>Не удалось отправить подсказку. Проверьте, что бот знает вас "
                                                    + "(необходимо выполнить</i> /start <i>в личке с ним) и он не заблокирован.</i>",
                                            ctx.getChatId(), ParseMode.HTML));
                        }
                    }

                    @Override
                    public void onSuccessResult(AtomicResponse<me.nizheg.telegram.bot.api.model.Message> result) {
                        HintCommand.super.sendCallbackResponse(ctx);
                    }
                });
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
}

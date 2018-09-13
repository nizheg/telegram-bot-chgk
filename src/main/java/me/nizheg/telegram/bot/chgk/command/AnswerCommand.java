package me.nizheg.telegram.bot.chgk.command;

import java.util.function.Supplier;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.chgk.command.exception.NoTaskException;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.domain.HintResult;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.exception.GameException;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.util.AnswerSender;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;

/**
 * @author Nikolay Zhegalin
 */
@UserInChannel
@ChatActive
public class AnswerCommand extends ChatCommand {

    private final ChatService chatService;
    private final ChatGameService chatGameService;
    private final AnswerSender answerSender;

    public AnswerCommand(
            @NonNull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @NonNull ChatService chatService,
            @NonNull ChatGameService chatGameService,
            @NonNull AnswerSender answerSender) {
        super(telegramApiClientSupplier);
        this.chatService = chatService;
        this.chatGameService = chatGameService;
        this.answerSender = answerSender;
    }

    protected ChatService getChatService() {
        return chatService;
    }

    protected ChatGameService getChatGameService() {
        return chatGameService;
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        Long taskId = parseTaskId(ctx);
        try {
            ChatGame chatGame = chatGameService.getGame(new Chat(ctx.getChat()));
            HintResult hintForTask = chatGame.getHintForTask(new Chat(ctx.getChat()), taskId);
            Task task = hintForTask.getTask().orElseThrow(NoTaskException::new);
            TelegramApiClient telegramApiClient = getTelegramApiClient();
            answerSender.sendAnswer(task, hintForTask.isTaskCurrent(), ctx.getChatId(),
                    new CallbackRequestDefaultCallback<>(ctx, telegramApiClient));
        } catch (GameException e) {
            throw new CommandException(e.getMessage(), e);
        }
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
        return "answer";
    }

    @Override
    public String getDescription() {
        return "/answer - получить ответ на текущий вопрос";
    }
}

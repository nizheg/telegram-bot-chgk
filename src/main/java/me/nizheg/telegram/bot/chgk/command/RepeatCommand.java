package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.Validate;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.api.model.ReplyMarkup;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.chgk.command.exception.NoTaskException;
import me.nizheg.telegram.bot.chgk.domain.AutoChatGame;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.util.TaskSender;
import me.nizheg.telegram.bot.chgk.util.WarningSender;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.util.Emoji;
import me.nizheg.telegram.util.TelegramApiUtil;

/**
 * @author Nikolay Zhegalin
 */
@UserInChannel
@ChatActive
public class RepeatCommand extends ChatCommand {

    private final ChatService chatService;
    private final ChatGameService chatGameService;
    private final TaskSender taskSender;
    private final WarningSender warningSender;

    public RepeatCommand(
            @Nonnull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @Nonnull ChatService chatService,
            @Nonnull ChatGameService chatGameService,
            @Nonnull TaskSender taskSender,
            @Nonnull WarningSender warningSender) {
        super(telegramApiClientSupplier);
        Validate.notNull(chatService, "chatService should be defined");
        Validate.notNull(chatGameService, "chatGameService should be defined");
        Validate.notNull(taskSender, "taskSender should be defined");
        Validate.notNull(warningSender, "warningSender should be defined");
        this.chatService = chatService;
        this.chatGameService = chatGameService;
        this.taskSender = taskSender;
        this.warningSender = warningSender;
    }

    protected ChatService getChatService() {
        return chatService;
    }

    protected ChatGameService getChatGameService() {
        return chatGameService;
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        Long chatId = ctx.getChatId();
        ChatGame chatGame = chatGameService.getGame(new Chat(ctx.getChat()));
        Optional<Task> currentTaskOptional = chatGame.repeatTask();
        if (currentTaskOptional.isPresent()) {
            Task currentTask = currentTaskOptional.get();
            long currentTaskId = currentTask.getId();
            if (logger.isDebugEnabled()) {
                logger.debug("Repeat [" + currentTaskId + "]");
            }
            ReplyMarkup replyMarkup;
            if (ctx.isPrivateChat()) {
                replyMarkup = TelegramApiUtil.createInlineButtonMarkup("Ответ", "answer " + currentTaskId,
                        "Дальше", "next " + currentTaskId);
            } else {
                replyMarkup = TelegramApiUtil.createInlineButtonMarkup("Подсказка", "hint " + currentTaskId,
                        "Дальше", "next " + currentTaskId);
            }
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append(Emoji.BLACK_QUESTION_MARK_ORNAMENT + "<b>Повторяю вопрос</b>\n");
            TelegramApiClient telegramApiClient = getTelegramApiClient();
            taskSender.sendTaskText(messageBuilder, currentTask, chatId, replyMarkup,
                    new CallbackRequestDefaultCallback<>(ctx, telegramApiClient));
            if (chatGame instanceof AutoChatGame) {
                int timeLeft = ((AutoChatGame) chatGame).getTimeLeft();
                if (timeLeft > 0) {
                    warningSender.sendTimeWarning(new Chat(ctx.getChat()), timeLeft);
                }
            }
        } else {
            throw new NoTaskException();
        }
    }

    @Override
    public void sendCallbackResponse(CommandContext ctx) {
    }

    @Override
    public String getCommandName() {
        return "repeat";
    }

    @Override
    public String getDescription() {
        return "/repeat - повторить ещё раз текущий вопрос";
    }
}

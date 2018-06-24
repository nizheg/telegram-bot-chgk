package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.Validate;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.api.model.ReplyMarkup;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.chgk.domain.AutoChatGame;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.exception.NoTaskException;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.util.TaskSender;
import me.nizheg.telegram.bot.chgk.util.WarningSender;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.util.Emoji;
import me.nizheg.telegram.util.TelegramApiUtil;

/**
 * @author Nikolay Zhegalin
 */
public class RepeatCommand extends ChatGameCommand {

    private final ChatService chatService;
    private final ChatGameService chatGameService;
    private final TaskSender taskSender;
    private final WarningSender warningSender;

    public RepeatCommand(
            @Nonnull TelegramApiClient telegramApiClient,
            @Nonnull ChatService chatService,
            @Nonnull ChatGameService chatGameService,
            @Nonnull TaskSender taskSender,
            @Nonnull WarningSender warningSender) {
        super(telegramApiClient);
        Validate.notNull(chatService, "chatService should be defined");
        Validate.notNull(chatGameService, "chatGameService should be defined");
        Validate.notNull(taskSender, "taskSender should be defined");
        Validate.notNull(warningSender, "warningSender should be defined");
        this.chatService = chatService;
        this.chatGameService = chatGameService;
        this.taskSender = taskSender;
        this.warningSender = warningSender;
    }

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
        Task currentTask = chatGame.repeatTask();
        if (currentTask != null) {
            logger.debug("Repeat [" + currentTask.getId() + "]");
            ReplyMarkup replyMarkup = null;
            if (ctx.isPrivateChat()) {
                replyMarkup = TelegramApiUtil.createInlineButtonMarkup("Ответ", "answer " + currentTask.getId(),
                        "Дальше", "next");
            } else {
                replyMarkup = TelegramApiUtil.createInlineButtonMarkup("Подсказка", "hint " + currentTask.getId(),
                        "Дальше", "next");
            }
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append(Emoji.BLACK_QUESTION_MARK_ORNAMENT + "<b>Повторяю вопрос</b>\n");
            taskSender.sendTaskText(messageBuilder, currentTask, chatId, replyMarkup);
            if (chatGame instanceof AutoChatGame) {
                int timeLeft = ((AutoChatGame) chatGame).getTimeLeft();
                if (timeLeft > 0) {
                    warningSender.sendTimeWarning(new Chat(ctx.getChat()), timeLeft);
                }
            }
        } else {
            throw new NoTaskException(chatId);
        }
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

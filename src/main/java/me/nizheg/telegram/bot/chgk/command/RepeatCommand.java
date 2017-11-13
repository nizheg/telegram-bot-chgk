package me.nizheg.telegram.bot.chgk.command;

import me.nizheg.telegram.bot.chgk.domain.AutoChatGame;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.exception.NoTaskException;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.util.TaskSender;
import me.nizheg.telegram.bot.chgk.util.WarningSender;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.model.ReplyMarkup;
import me.nizheg.telegram.service.TelegramApiClient;
import me.nizheg.telegram.util.Emoji;
import me.nizheg.telegram.util.TelegramApiUtil;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class RepeatCommand extends ChatGameCommand {

    @Autowired
    private ChatService chatService;
    @Autowired
    private TaskSender taskSender;
    @Autowired
    private WarningSender warningSender;

    public RepeatCommand(TelegramApiClient telegramApiClient) {
        super(telegramApiClient);
    }

    @Override
    protected ChatService getChatService() {
        return chatService;
    }

    @Override
    protected void executeChatGame(CommandContext ctx, ChatGame chatGame) throws CommandException {
        Long chatId = ctx.getChatId();
        Task currentTask = chatGame.repeatTask();
        if (currentTask != null) {
            logger.debug("Repeat [" + currentTask.getId() + "]");
            ReplyMarkup replyMarkup = null;
            if (!(chatGame instanceof AutoChatGame)) {
                if (ctx.isPrivateChat()) {
                    replyMarkup = TelegramApiUtil.createInlineButtonMarkup("Ответ", "answer " + currentTask.getId(), "Дальше", "next");
                } else {
                    replyMarkup = TelegramApiUtil.createInlineButtonMarkup("Подсказка", "hint " + currentTask.getId(), "Дальше", "next");
                }
            }
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append(Emoji.BLACK_QUESTION_MARK_ORNAMENT + "<b>Повторяю вопрос</b>\n");
            taskSender.sendTaskText(messageBuilder, currentTask, chatId, replyMarkup);
            if (chatGame instanceof AutoChatGame) {
                warningSender.sendTimeWarning(new Chat(ctx.getChat()), ((AutoChatGame) chatGame).getTimeToNextTask());
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

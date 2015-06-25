package me.nizheg.chgk.command;

import me.nizheg.chgk.domain.ChatGame;
import me.nizheg.chgk.domain.HintResult;
import me.nizheg.chgk.dto.Chat;
import me.nizheg.chgk.dto.composite.Task;
import me.nizheg.chgk.exception.NoTaskException;
import me.nizheg.chgk.service.ChatService;
import me.nizheg.chgk.util.AnswerSender;
import me.nizheg.telegram.bot.service.command.CommandContext;
import me.nizheg.telegram.bot.service.command.CommandException;
import me.nizheg.telegram.model.ParseMode;
import me.nizheg.telegram.service.TelegramApiClient;
import me.nizheg.telegram.service.TelegramApiException;
import me.nizheg.telegram.service.param.Message;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class HintCommand extends ChatGameCommand {

    @Autowired
    private ChatService chatService;
    @Autowired
    private AnswerSender answerSender;

    public HintCommand(TelegramApiClient telegramApiClient) {
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
        HintResult hintForTask = chatGame.getHintForTask(new Chat(ctx.getFrom()), taskId);
        if (hintForTask.getTask() != null) {
            sendAnswerToUser(ctx, hintForTask.getTask());
        } else {
            throw new NoTaskException(chatId);
        }
    }

    private void sendAnswerToUser(CommandContext ctx, Task currentTask) {
        try {
            StringBuilder messageBuilder = new StringBuilder("<b>Ответ:</b>\n");
            answerSender.sendAnswerOfTask(messageBuilder, currentTask, ctx.getFrom().getId(), null);
        } catch (TelegramApiException ex) {
            telegramApiClient
                    .sendMessage(new Message(
                            "<i>Не удалось отправить подсказку. Проверьте, что бот знает вас (необходимо выполнить</i> /start <i>в личке с ним) и он не заблокирован.</i>",
                            ctx.getChatId(), ParseMode.HTML));
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
        return "hint";
    }

    @Override
    public String getDescription() {
        return "/hint - получить ответ на текущий вопрос в личку";
    }
}

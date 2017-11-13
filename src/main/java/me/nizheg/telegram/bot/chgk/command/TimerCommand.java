package me.nizheg.telegram.bot.chgk.command;

import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.model.ParseMode;
import me.nizheg.telegram.service.TelegramApiClient;
import me.nizheg.telegram.service.param.Message;
import me.nizheg.telegram.util.Emoji;
import me.nizheg.telegram.util.TelegramApiUtil;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Nikolay Zhegalin
 */
public class TimerCommand extends ChatCommand {

    private final static String COMMAND_NAME_TIMER_SET = "timer";
    private final static String COMMAND_NAME_TIMER_RESET = "timer_stop";
    private static final int MINUTE = 60;
    private static final int DEFAULT_TIMEOUT_MINUTES = 1;
    private static final int MAX_TIMEOUT_MINUTES = 24 * 60;
    @Autowired
    private ChatService chatService;

    public TimerCommand(TelegramApiClient telegramApiClient) {
        super(telegramApiClient);
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        if (COMMAND_NAME_TIMER_SET.equals(ctx.getCommand())) {
            setTimer(ctx);
        } else if (COMMAND_NAME_TIMER_RESET.equals(ctx.getCommand())) {
            resetTimer(ctx);
        }
    }

    private void setTimer(CommandContext ctx) throws CommandException {
        String option = ctx.getText();
        int timeoutMinutes = DEFAULT_TIMEOUT_MINUTES;
        if (StringUtils.isNotBlank(option)) {
            boolean isValueCorrect = true;
            try {
                timeoutMinutes = Integer.parseInt(option);
            } catch (NumberFormatException ex) {
                isValueCorrect = false;
            }
            if (!isValueCorrect || timeoutMinutes < 0 || timeoutMinutes > MAX_TIMEOUT_MINUTES) {
                throw new CommandException(new Message("<i>Введите в качестве значения целое число не больше " + MAX_TIMEOUT_MINUTES + " </i>",
                        ctx.getChatId(), ParseMode.HTML));
            }
        }
        chatService.setTimer(ctx.getChatId(), timeoutMinutes * MINUTE);
        telegramApiClient.sendMessage(new Message(Emoji.BELL + " <i>Установлен таймер автоматической выдачи вопросов в " + timeoutMinutes + " мин.</i>", ctx
                .getChatId(), ParseMode.HTML, true, null, TelegramApiUtil.createInlineButtonMarkup("Новый вопрос", "next", "Повторить вопрос", "repeat")));
    }

    private void resetTimer(CommandContext ctx) {
        chatService.clearTimer(ctx.getChatId());
        telegramApiClient.sendMessage(new Message(Emoji.BELL_WITH_CANCELLATION_STROKE + " <i>Автоматическая выдача вопросов выключена</i>", ctx.getChatId(),
                ParseMode.HTML, false));
    }

    @Override
    public String getCommandName() {
        return "timer(_stop)?";
    }

    @Override
    public String getDescription() {
        return "/" + COMMAND_NAME_TIMER_SET + " <количество минут> - запустить таймер (по умолчанию количество минут = 1)\n" //
                + "/" + COMMAND_NAME_TIMER_RESET + " - выключить автоматическую выдачу вопросов";
    }
}

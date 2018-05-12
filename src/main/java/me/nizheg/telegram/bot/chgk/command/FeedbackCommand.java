package me.nizheg.telegram.bot.chgk.command;

import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.dto.FeedbackResult;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.service.FeedbackService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class FeedbackCommand extends ChatCommand {

    @Autowired
    private FeedbackService feedbackService;

    public FeedbackCommand(TelegramApiClient telegramApiClient) {
        super(telegramApiClient);
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        String feedbackText = ctx.getText();
        if (StringUtils.isBlank(feedbackText)) {
            throw new CommandException(new Message(
                    "<i>Вы забыли написать ваш отзыв. Чтобы это сделать, нужно вызвать команду в виде</i> /feedback <code>отзыв</code>", ctx.getChatId(),
                    ParseMode.HTML));
        }
        FeedbackResult feedbackResult = feedbackService.registerFeedback(new TelegramUser(ctx.getMessage().getFrom()), feedbackText);
        if (feedbackResult.getErrorDescription() != null) {
            getTelegramApiClient().sendMessage(new Message(feedbackResult.getErrorDescription(), ctx.getChatId()));
        } else if (feedbackResult.getLink() != null) {
            getTelegramApiClient().sendMessage(new Message("<i>Спасибо. Ваш отзыв получен и размещён в обсуждении</i> " + feedbackResult.getLink(), ctx.getChatId(),
                    ParseMode.HTML));
        } else {
            getTelegramApiClient().sendMessage(new Message("<i>Спасибо за отзыв.</i>", ctx.getChatId(), ParseMode.HTML));
        }
    }

    @Override
    public String getCommandName() {
        return "feedback";
    }

    @Override
    public String getDescription() {
        return "/feedback отзыв - отправить отзыв: сообщение будет отправлено в группу сообщества с указанием информации об отправителе.";
    }
}

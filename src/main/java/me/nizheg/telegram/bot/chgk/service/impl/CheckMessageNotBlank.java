package me.nizheg.telegram.bot.chgk.service.impl;

import org.apache.commons.lang3.StringUtils;

import me.nizheg.telegram.bot.chgk.command.MessageWithText;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.bot.service.impl.PreconditionChainStep;
import me.nizheg.telegram.bot.service.impl.PreconditionResult;

/**
 * @author Nikolay Zhegalin
 */
public class CheckMessageNotBlank extends PreconditionChainStep {

    @Override
    protected PreconditionResult doCheck(ChatCommand commandHandler, CommandContext context) throws CommandException {
        boolean isSuccess = true;
        if (commandHandler != null && commandHandler.getClass().getAnnotation(MessageWithText.class) != null) {
            isSuccess = StringUtils.isNotBlank(context.getText());
        }
        return new PreconditionResult(isSuccess, "Check message not blank");
    }
}

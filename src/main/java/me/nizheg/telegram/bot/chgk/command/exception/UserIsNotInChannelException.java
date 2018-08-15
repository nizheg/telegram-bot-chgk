package me.nizheg.telegram.bot.chgk.command.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.api.model.User;
import me.nizheg.telegram.bot.command.CommandException;

/**
 * @author Nikolay Zhegalin
 */
@RequiredArgsConstructor
@Getter
public class UserIsNotInChannelException extends CommandException {

    private static final long serialVersionUID = 2971272944941652219L;
    private final User user;
    private final String channelName;

}

package me.nizheg.telegram.bot.chgk.domain;

import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;

/**
 * @author Nikolay Zhegalin
 */
public interface NextTaskOperation {
    void sendNextTask(ChatGame chatGame);

    public void sendAnswerOfPreviousTask(Chat chat, Task task);
}

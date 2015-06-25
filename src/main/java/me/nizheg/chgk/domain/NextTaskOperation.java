package me.nizheg.chgk.domain;

import me.nizheg.chgk.dto.Chat;
import me.nizheg.chgk.dto.composite.Task;

/**
 * @author Nikolay Zhegalin
 */
public interface NextTaskOperation {
    void sendNextTask(ChatGame chatGame);

    public void sendAnswerOfPreviousTask(Chat chat, Task task);
}

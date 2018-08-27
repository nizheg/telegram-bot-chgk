package me.nizheg.telegram.bot.chgk.work;

import java.util.List;

import me.nizheg.telegram.bot.chgk.work.data.ForwardMessageData;
import me.nizheg.telegram.bot.chgk.work.data.SendMessageData;

public interface WorkService {

    void forwardMessageToActiveChats(ForwardMessageData forwardMessageData);

    void sendMessageToActiveChats(SendMessageData sendMessageData);

    List<WorkDescription> getWorks(int count, WorkStatus status);

    void changeStatus(WorkDescription workDescription, WorkStatus status);
}

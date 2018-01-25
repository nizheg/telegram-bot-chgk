package me.nizheg.telegram.bot.chgk.web;

import java.security.Principal;

import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.chgk.dto.BroadcastStatus;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.service.BroadcastSender;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.service.TaskService;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;
import me.nizheg.telegram.bot.chgk.util.TaskSender;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
@RestController
@RequestMapping("api/message")
public class MessageController {
    public static final String RECEIVER_ALL = "all";
    private static final String RECEIVER_ME = "me";
    @Autowired
    private BroadcastSender broadcastSender;
    @Autowired
    private TelegramUserService telegramUserService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private TelegramApiClient telegramApiClient;
    @Autowired
    private TaskSender taskSender;

    private Object lock = new Object();
    private BroadcastStatus broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.NOT_STARTED);

    @RequestMapping(method = RequestMethod.POST)
    public BroadcastStatus send(@RequestBody Message message, Principal principal) {
        if (message.getReceiver().equals(RECEIVER_ME)) {
            if (principal != null && StringUtils.isNotBlank(principal.getName())) {
                TelegramUser telegramUser = telegramUserService.getByUsername(principal.getName());
                if (telegramUser == null) {
                    throw new IllegalStateException("Unable to calculate current user chat");
                }
                Long taskId = message.getTaskId();
                if (taskId != null) {
                    Task currentTask = chatService.getGame(new Chat(telegramUser.getId(), true)).setCurrentTask(taskId);
                    taskSender.sendTaskText(currentTask, telegramUser.getId());
                } else {
                    telegramApiClient.sendMessage(new me.nizheg.telegram.bot.api.service.param.Message(message.getText(), telegramUser.getId(), ParseMode.HTML, true));
                }
                return new BroadcastStatus(BroadcastStatus.Status.FINISHED);
            }
            return new BroadcastStatus(BroadcastStatus.Status.REJECTED, "Пользователь не определен");
        } else if (message.getReceiver().equals(RECEIVER_ALL)) {
            synchronized (lock) {
                if (BroadcastStatus.Status.IN_PROCESS.equals(broadcastStatus.getStatus())) {
                    return new BroadcastStatus(BroadcastStatus.Status.REJECTED, "Предыдущая задача отправки ещё не завершена.");
                } else {
                    broadcastStatus = broadcastSender.sendMessage(message.getText());
                    return broadcastStatus;
                }
            }
        } else {
            throw new UnsupportedOperationException("Not supported now");
        }
    }

    @RequestMapping(value = "/status", method = RequestMethod.POST)
    public BroadcastStatus setStatus(@RequestBody BroadcastStatus status) {
        if (BroadcastStatus.Status.CANCELLED.equals(status.getStatus())) {
            synchronized (lock) {
                broadcastStatus.setStatus(BroadcastStatus.Status.CANCELLED);
            }
        }
        return broadcastStatus;
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public BroadcastStatus getStatus() {
        synchronized (lock) {
            return broadcastStatus;
        }
    }

    public static class Message {
        private String receiver;
        private String text;
        private Long taskId;

        public String getReceiver() {
            return receiver;
        }

        public void setReceiver(String receiver) {
            this.receiver = receiver;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Long getTaskId() {
            return taskId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }
    }
}

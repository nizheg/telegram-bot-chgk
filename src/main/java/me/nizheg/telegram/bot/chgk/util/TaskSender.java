package me.nizheg.telegram.bot.chgk.util;

import java.util.Iterator;
import java.util.List;

import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.model.PhotoSize;
import me.nizheg.telegram.bot.api.model.ReplyMarkup;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.InputFile;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.api.service.param.Photo;
import me.nizheg.telegram.bot.chgk.dto.AttachedPicture;
import me.nizheg.telegram.bot.chgk.dto.LightTask;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.service.PictureService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nikolay Zhegalin
 */
@Component
public class TaskSender {

    @Autowired
    private TelegramApiClient telegramApiClient;
    @Autowired
    private PictureService pictureService;

    public void sendTaskText(StringBuilder textBuilder, Task task, Long chatId, ReplyMarkup replyMarkup) {
        if (task == null) {
            return;
        }
        String taskText = task.getText();
        textBuilder.append(taskText);
        int halfOfTaskLength = taskText.length() / 2;
        List<AttachedPicture> attachedPictures = task.getTextPictures();
        int attachedPicturesSize = attachedPictures.size();
        int i = 0;
        while (i < attachedPicturesSize && attachedPictures.get(i).getPosition() <= halfOfTaskLength) {
            ++i;
        }
        sendAttachedPictures(chatId, attachedPictures.subList(0, i), null);
        List<AttachedPicture> attachedPicturesPart2 = attachedPictures.subList(i, attachedPicturesSize);
        ReplyMarkup messageReplyMarkup = attachedPicturesPart2.isEmpty() ? replyMarkup : null;
        telegramApiClient.sendMessage(new Message(textBuilder.toString(), chatId, ParseMode.HTML, true, null, messageReplyMarkup));
        sendAttachedPictures(chatId, attachedPicturesPart2, replyMarkup);
    }

    public void sendTaskText(Task task, Long chatId) {
        sendTaskText(new StringBuilder(), task, chatId, null);
    }

    public void sendTaskText(Task task, Long chatId, ReplyMarkup replyMarkup) {
        sendTaskText(new StringBuilder(), task, chatId, replyMarkup);
    }

    public void sendTaskComment(StringBuilder messageBuilder, Task task, Long chatId, ReplyMarkup replyMarkup) {
        if (task == null) {
            return;
        }
        messageBuilder.append("\n\n<b>Комментарий:</b>\n" + getComment(task));
        List<AttachedPicture> attachedPictures = task.getCommentPictures();
        Message sendingMessage = new Message(messageBuilder.toString(), chatId, ParseMode.HTML, true);
        if (attachedPictures.isEmpty()) {
            sendingMessage.setReplyMarkup(replyMarkup);
        }
        telegramApiClient.sendMessage(sendingMessage);
        sendAttachedPictures(chatId, attachedPictures, replyMarkup);
    }

    private void sendAttachedPictures(Long chatId, List<AttachedPicture> attachedPictures, ReplyMarkup replyMarkup) {
        for (Iterator<AttachedPicture> iterator = attachedPictures.iterator(); iterator.hasNext();) {
            AttachedPicture attachedPicture = iterator.next();
            InputFile photo;
            if (attachedPicture.getTelegramFileId() != null) {
                photo = new InputFile(attachedPicture.getTelegramFileId());
            } else {
                photo = new InputFile(attachedPicture.getSourceUrl());
            }
            Photo sendingPhoto = new Photo(photo, new ChatId(chatId), attachedPicture.getCaption());
            if (!iterator.hasNext()) {
                sendingPhoto.setReplyMarkup(replyMarkup);
            }
            me.nizheg.telegram.bot.api.model.Message sentMessage = telegramApiClient.sendPhoto(sendingPhoto).getResult();

            if (attachedPicture.getTelegramFileId() == null) {
                List<PhotoSize> photos = sentMessage.getPhoto();
                if (photos != null && !photos.isEmpty()) {
                    attachedPicture.setTelegramFileId(photos.get(0).getFileId());
                    pictureService.update(attachedPicture);
                }
            }
        }
    }

    private String getComment(LightTask task) {
        String comment = "не указан";
        if (StringUtils.isNotBlank(task.getComment())) {
            comment = task.getComment();
        }
        return comment;
    }

}

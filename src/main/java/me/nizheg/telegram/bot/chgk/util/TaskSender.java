package me.nizheg.telegram.bot.chgk.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.model.AtomicResponse;
import me.nizheg.telegram.bot.api.model.Callback;
import me.nizheg.telegram.bot.api.model.ErrorResponse;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.model.PhotoSize;
import me.nizheg.telegram.bot.api.model.ReplyMarkup;
import me.nizheg.telegram.bot.api.model.Response;
import me.nizheg.telegram.bot.api.model.SuccessCallback;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.InputFile;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.api.service.param.Photo;
import me.nizheg.telegram.bot.chgk.dto.AttachedPicture;
import me.nizheg.telegram.bot.chgk.dto.LightTask;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.service.PictureService;

/**
 * @author Nikolay Zhegalin
 */
@Component
public class TaskSender {

    private final Supplier<TelegramApiClient> telegramApiClientSupplier;
    private final PictureService pictureService;

    public TaskSender(
            @NonNull Supplier<TelegramApiClient> telegramApiClientSupplier,
            PictureService pictureService) {
        this.telegramApiClientSupplier = telegramApiClientSupplier;
        this.pictureService = pictureService;
    }

    public TelegramApiClient getTelegramApiClient() {
        return telegramApiClientSupplier.get();
    }

    public void sendTaskText(
            StringBuilder textBuilder, Task task, Long chatId, ReplyMarkup replyMarkup,
            @NonNull Callback<AtomicResponse<me.nizheg.telegram.bot.api.model.Message>> callback) {
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
        TelegramApiClient telegramApiClient = getTelegramApiClient();
        sendAttachedPictures(telegramApiClient, chatId, attachedPictures.subList(0, i), null);
        List<AttachedPicture> attachedPicturesPart2 = attachedPictures.subList(i, attachedPicturesSize);
        ReplyMarkup messageReplyMarkup = attachedPicturesPart2.isEmpty() ? replyMarkup : null;
        telegramApiClient.sendMessage(
                Message.safeMessageBuilder()
                        .text(textBuilder.toString())
                        .chatId(new ChatId(chatId))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .replyMarkup(messageReplyMarkup)
                        .build())
                .setCallback(callback);
        sendAttachedPictures(telegramApiClient, chatId, attachedPicturesPart2, replyMarkup);
    }


    public void sendTaskText(Task task, Long chatId) {
        sendTaskText(new StringBuilder(), task, chatId, null, (errorResponse, httpStatus) -> {
        });
    }

    public void sendTaskComment(
            StringBuilder messageBuilder,
            Task task,
            Long chatId,
            ReplyMarkup replyMarkup,
            @NonNull Callback<AtomicResponse<me.nizheg.telegram.bot.api.model.Message>> callback) {
        if (task == null) {
            return;
        }
        messageBuilder.append("\n\n<b>Комментарий:</b>\n").append(getComment(task));
        List<AttachedPicture> attachedPictures = task.getCommentPictures();
        Message.SafeMessageBuilder sendingMessageBuilder = Message.safeMessageBuilder()
                .text(messageBuilder.toString())
                .chatId(new ChatId(chatId))
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true);
        if (attachedPictures.isEmpty()) {
            sendingMessageBuilder.replyMarkup(replyMarkup);
        }
        TelegramApiClient telegramApiClient = getTelegramApiClient();
        telegramApiClient.sendMessage(sendingMessageBuilder.build()).setCallback(
                new Callback<AtomicResponse<me.nizheg.telegram.bot.api.model.Message>>() {
                    @Override
                    public void onFailure(ErrorResponse errorResponse, HttpStatus httpStatus) {
                        callback.onFailure(errorResponse, httpStatus);
                    }

                    @Override
                    public void onSuccessResult(AtomicResponse<me.nizheg.telegram.bot.api.model.Message> result) {
                        sendAttachedPictures(telegramApiClient, chatId, attachedPictures, replyMarkup);
                        callback.onSuccessResult(result);
                    }
                });

    }

    private void sendAttachedPictures(
            TelegramApiClient telegramApiClient,
            Long chatId,
            List<AttachedPicture> attachedPictures,
            ReplyMarkup replyMarkup) {
        sendAttachedPictures(telegramApiClient, chatId, attachedPictures, replyMarkup, (errorResponse, httpStatus) ->
                telegramApiClient.sendMessage(Message.safeMessageBuilder()
                        .text("<i>Произошла непредвиденая ошибка при отправке изображения</i>")
                        .chatId(new ChatId(chatId))
                        .parseMode(ParseMode.HTML)
                        .build()));
    }

    private void sendAttachedPictures(
            TelegramApiClient telegramApiClient,
            Long chatId,
            List<AttachedPicture> attachedPictures,
            ReplyMarkup replyMarkup,
            Callback<AtomicResponse<me.nizheg.telegram.bot.api.model.Message>> callback) {
        for (Iterator<AttachedPicture> iterator = attachedPictures.iterator(); iterator.hasNext(); ) {
            AttachedPicture attachedPicture = iterator.next();
            InputFile photo;
            if (attachedPicture.getTelegramFileId() != null) {
                photo = new InputFile(attachedPicture.getTelegramFileId());
            } else {
                photo = new InputFile(attachedPicture.getSourceUrl());
            }
            Photo.PhotoBuilder sendingPhotoBuilder = Photo.builder()
                    .photo(photo)
                    .chatId(new ChatId(chatId))
                    .caption(attachedPicture.getCaption());
            if (!iterator.hasNext()) {
                sendingPhotoBuilder.replyMarkup(replyMarkup);
            }
            telegramApiClient.sendPhoto(sendingPhotoBuilder.build()).setCallback(
                    new Callback<AtomicResponse<me.nizheg.telegram.bot.api.model.Message>>() {
                        @Override
                        public void onFailure(ErrorResponse errorResponse, HttpStatus httpStatus) {
                            callback.onFailure(errorResponse, httpStatus);
                        }

                        @Override
                        public void onSuccessResult(AtomicResponse<me.nizheg.telegram.bot.api.model.Message> result) {
                            if (attachedPicture.getTelegramFileId() == null) {
                                List<PhotoSize> photos = result.getResult().getPhoto();
                                if (photos != null && !photos.isEmpty()) {
                                    attachedPicture.setTelegramFileId(photos.get(0).getFileId());
                                    pictureService.update(attachedPicture);
                                }
                            }
                            callback.onSuccessResult(result);
                        }
                    });

        }
    }

    private String getComment(LightTask task) {
        String comment = "не указан";
        if (StringUtils.isNotBlank(task.getComment())) {
            comment = task.getComment();
        }
        return comment;
    }

    private static <T extends Response> Callback<T> onSuccess(SuccessCallback<T> successCallback) {
        return new Callback<T>() {
            @Override
            public void onFailure(ErrorResponse errorResponse, HttpStatus httpStatus) {
            }

            @Override
            public void onSuccessResult(T result) {
                successCallback.onSuccessResult(result);
            }
        };
    }

}

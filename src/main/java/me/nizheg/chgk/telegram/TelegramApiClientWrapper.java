package me.nizheg.chgk.telegram;

import java.util.List;

import me.nizheg.chgk.dto.ChatError;
import me.nizheg.chgk.service.ChatService;
import me.nizheg.telegram.model.BooleanResponse;
import me.nizheg.telegram.model.ChatAction;
import me.nizheg.telegram.model.ChatMemberCollectionResponse;
import me.nizheg.telegram.model.ChatMemberResponse;
import me.nizheg.telegram.model.ChatResponse;
import me.nizheg.telegram.model.FileResponse;
import me.nizheg.telegram.model.GameHighScoreCollectionResponse;
import me.nizheg.telegram.model.IntegerResponse;
import me.nizheg.telegram.model.MessageOrBooleanResponse;
import me.nizheg.telegram.model.MessageResponse;
import me.nizheg.telegram.model.UpdateCollectionResponse;
import me.nizheg.telegram.model.UpdateType;
import me.nizheg.telegram.model.UserProfilePhotosResponse;
import me.nizheg.telegram.model.UserResponse;
import me.nizheg.telegram.model.WebhookInfoResponse;
import me.nizheg.telegram.service.TelegramApiClient;
import me.nizheg.telegram.service.TelegramApiException;
import me.nizheg.telegram.service.param.AnswerCallbackRequest;
import me.nizheg.telegram.service.param.Audio;
import me.nizheg.telegram.service.param.ChatId;
import me.nizheg.telegram.service.param.Contact;
import me.nizheg.telegram.service.param.Document;
import me.nizheg.telegram.service.param.EditedMessage;
import me.nizheg.telegram.service.param.ForwardingMessage;
import me.nizheg.telegram.service.param.Game;
import me.nizheg.telegram.service.param.GameHighScoreRequest;
import me.nizheg.telegram.service.param.GameScore;
import me.nizheg.telegram.service.param.InlineAnswer;
import me.nizheg.telegram.service.param.Location;
import me.nizheg.telegram.service.param.Message;
import me.nizheg.telegram.service.param.Photo;
import me.nizheg.telegram.service.param.Sticker;
import me.nizheg.telegram.service.param.UserProfilePhotosRequest;
import me.nizheg.telegram.service.param.Venue;
import me.nizheg.telegram.service.param.Video;
import me.nizheg.telegram.service.param.VideoNote;
import me.nizheg.telegram.service.param.Voice;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class TelegramApiClientWrapper implements TelegramApiClient {

    private TelegramApiClient telegramApiClient;
    @Autowired
    private ChatService chatService;
    private Log logger = LogFactory.getLog(getClass());

    public TelegramApiClientWrapper(TelegramApiClient telegramApiClient) {
        Validate.notNull(telegramApiClient);
        this.telegramApiClient = telegramApiClient;
    }

    @Override
    public UserResponse getMe() {
        return telegramApiClient.getMe();
    }

    @Override
    public WebhookInfoResponse getWebhookInfo() {
        return telegramApiClient.getWebhookInfo();
    }

    @Override
    public MessageResponse sendMessage(Message message) {
        try {
            return telegramApiClient.sendMessage(message);
        } catch (TelegramApiException ex) {
            storeFail(ex, message.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, message.getChatId());
            throw ex;
        }
    }

    @Override
    public MessageResponse forwardMessage(ForwardingMessage message) {
        try {
            return telegramApiClient.forwardMessage(message);
        } catch (TelegramApiException ex) {
            storeFail(ex, message.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, message.getChatId());
            throw ex;
        }
    }

    @Override
    public MessageResponse sendPhoto(Photo photo) {
        try {
            return telegramApiClient.sendPhoto(photo);
        } catch (TelegramApiException ex) {
            storeFail(ex, photo.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, photo.getChatId());
            throw ex;
        }
    }

    @Override
    public MessageResponse sendAudio(Audio audio) {
        try {
            return telegramApiClient.sendAudio(audio);
        } catch (TelegramApiException ex) {
            storeFail(ex, audio.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, audio.getChatId());
            throw ex;
        }
    }

    @Override
    public MessageResponse sendDocument(Document document) {
        try {
            return telegramApiClient.sendDocument(document);
        } catch (TelegramApiException ex) {
            storeFail(ex, document.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, document.getChatId());
            throw ex;
        }
    }

    @Override
    public MessageResponse sendSticker(Sticker sticker) {
        try {
            return telegramApiClient.sendSticker(sticker);
        } catch (TelegramApiException ex) {
            storeFail(ex, sticker.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, sticker.getChatId());
            throw ex;
        }
    }

    @Override
    public MessageResponse sendVideo(Video video) {
        try {
            return telegramApiClient.sendVideo(video);
        } catch (TelegramApiException ex) {
            storeFail(ex, video.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, video.getChatId());
            throw ex;
        }
    }

    @Override
    public MessageResponse sendVoice(Voice voice) {
        try {
            return telegramApiClient.sendVoice(voice);
        } catch (TelegramApiException ex) {
            storeFail(ex, voice.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, voice.getChatId());
            throw ex;
        }
    }

    @Override
    public MessageResponse sendVideoNote(VideoNote videoNote) {
        try {
            return telegramApiClient.sendVideoNote(videoNote);
        } catch (TelegramApiException ex) {
            storeFail(ex, videoNote.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, videoNote.getChatId());
            throw ex;
        }
    }

    @Override
    public BooleanResponse sendChatAction(ChatAction chatAction, ChatId chatId) {
        return telegramApiClient.sendChatAction(chatAction, chatId);
    }

    @Override
    public UserProfilePhotosResponse getUserProfilePhotos(UserProfilePhotosRequest userProfilePhotosRequest) {
        return telegramApiClient.getUserProfilePhotos(userProfilePhotosRequest);
    }

    @Override
    public FileResponse getFile(String fileId) {
        return telegramApiClient.getFile(fileId);
    }

    @Override
    public BooleanResponse kickChatMember(ChatId chatId, Long userId) {
        return telegramApiClient.kickChatMember(chatId, userId);
    }

    @Override
    public BooleanResponse leaveChat(ChatId chatId) {
        return telegramApiClient.leaveChat(chatId);
    }

    @Override
    public BooleanResponse unbanChatMember(ChatId chatId, Long userId) {
        return telegramApiClient.unbanChatMember(chatId, userId);
    }

    @Override
    public ChatResponse getChat(ChatId chatId) {
        return telegramApiClient.getChat(chatId);
    }

    @Override
    public ChatMemberCollectionResponse getChatAdministrators(ChatId chatId) {
        return telegramApiClient.getChatAdministrators(chatId);
    }

    @Override
    public IntegerResponse getChatMembersCount(ChatId chatId) {
        return telegramApiClient.getChatMembersCount(chatId);
    }

    @Override
    public ChatMemberResponse getChatMember(ChatId chatId, Long userId) {
        return telegramApiClient.getChatMember(chatId, userId);
    }

    @Override
    public BooleanResponse answerCallbackQuery(AnswerCallbackRequest answerCallbackRequest) {
        return telegramApiClient.answerCallbackQuery(answerCallbackRequest);
    }

    @Override
    public UpdateCollectionResponse getUpdates(Long offset, Integer limit, Integer timeout, List<UpdateType> allowedUpdates) {
        return telegramApiClient.getUpdates(offset, limit, timeout, null);
    }

    @Override
    public MessageOrBooleanResponse editMessageText(EditedMessage editedMessage) {
        return telegramApiClient.editMessageText(editedMessage);
    }

    @Override
    public MessageOrBooleanResponse editMessageCaption(EditedMessage editedMessage) {
        return telegramApiClient.editMessageCaption(editedMessage);
    }

    @Override
    public MessageOrBooleanResponse editMessageReplyMarkup(EditedMessage editedMessage) {
        return telegramApiClient.editMessageReplyMarkup(editedMessage);
    }

    @Override
    public BooleanResponse deleteMessage(ChatId chatId, Long messageId) {
        return telegramApiClient.deleteMessage(chatId, messageId);
    }

    @Override
    public BooleanResponse answerInlineQuery(InlineAnswer inlineAnswer) {
        return telegramApiClient.answerInlineQuery(inlineAnswer);
    }

    @Override
    public MessageResponse sendGame(Game game) {
        try {
            return telegramApiClient.sendGame(game);
        } catch (TelegramApiException ex) {
            storeFail(ex, game.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, game.getChatId());
            throw ex;
        }
    }

    @Override
    public MessageOrBooleanResponse setGameScore(GameScore gameScore) {
        try {
            return telegramApiClient.setGameScore(gameScore);
        } catch (TelegramApiException ex) {
            storeFail(ex, gameScore.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, gameScore.getChatId());
            throw ex;
        }
    }

    @Override
    public GameHighScoreCollectionResponse getGameHighScores(GameHighScoreRequest gameHighScoreRequest) {
        try {
            return telegramApiClient.getGameHighScores(gameHighScoreRequest);
        } catch (TelegramApiException ex) {
            storeFail(ex, gameHighScoreRequest.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, gameHighScoreRequest.getChatId());
            throw ex;
        }
    }

    @Override
    public MessageResponse sendLocation(Location location) {
        try {
            return telegramApiClient.sendLocation(location);
        } catch (TelegramApiException ex) {
            storeFail(ex, location.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, location.getChatId());
            throw ex;
        }
    }

    @Override
    public MessageResponse sendVenue(Venue venue) {
        try {
            return telegramApiClient.sendVenue(venue);
        } catch (TelegramApiException ex) {
            storeFail(ex, venue.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, venue.getChatId());
            throw ex;
        }
    }

    @Override
    public MessageResponse sendContact(Contact contact) {
        try {
            return telegramApiClient.sendContact(contact);
        } catch (TelegramApiException ex) {
            storeFail(ex, contact.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, contact.getChatId());
            throw ex;
        }
    }

    private void storeFail(RuntimeException ex, ChatId chatId) {
        ChatError chatError = new ChatError();
        if (chatId.getChatId() == null) {
            return;
        }
        chatError.setChatId(chatId.getChatId());
        chatError.setCode(ex.getClass().getSimpleName());
        chatError.setDescription(ex.getMessage());
        storeChatError(chatError);
        logger.error("Sending failed for: " + chatId, ex);
    }

    private void storeChatError(ChatError chatError) {
        try {
            chatService.storeChatError(chatError);
        } catch (RuntimeException e) {
            logger.error("Unable to store error: [" + chatError.getChatId() + ", " + chatError.getCode() + ", " + chatError.getDescription() + "]", e);
        }
    }
}

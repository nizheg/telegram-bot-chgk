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
import me.nizheg.telegram.model.StickerSetResponse;
import me.nizheg.telegram.model.StringResponse;
import me.nizheg.telegram.model.UpdateCollectionResponse;
import me.nizheg.telegram.model.UpdateType;
import me.nizheg.telegram.model.UserProfilePhotosResponse;
import me.nizheg.telegram.model.UserResponse;
import me.nizheg.telegram.model.WebhookInfoResponse;
import me.nizheg.telegram.service.TelegramApiClient;
import me.nizheg.telegram.service.TelegramApiException;
import me.nizheg.telegram.service.param.AddingToSetSticker;
import me.nizheg.telegram.service.param.AnswerCallbackRequest;
import me.nizheg.telegram.service.param.Audio;
import me.nizheg.telegram.service.param.ChatId;
import me.nizheg.telegram.service.param.Contact;
import me.nizheg.telegram.service.param.Document;
import me.nizheg.telegram.service.param.EditedLiveLocation;
import me.nizheg.telegram.service.param.EditedMessage;
import me.nizheg.telegram.service.param.ForwardingMessage;
import me.nizheg.telegram.service.param.Game;
import me.nizheg.telegram.service.param.GameHighScoreRequest;
import me.nizheg.telegram.service.param.GameScore;
import me.nizheg.telegram.service.param.InlineAnswer;
import me.nizheg.telegram.service.param.InputFile;
import me.nizheg.telegram.service.param.Invoice;
import me.nizheg.telegram.service.param.KickedChatMember;
import me.nizheg.telegram.service.param.Location;
import me.nizheg.telegram.service.param.Message;
import me.nizheg.telegram.service.param.NewStickerSet;
import me.nizheg.telegram.service.param.Photo;
import me.nizheg.telegram.service.param.PinnedChatMessage;
import me.nizheg.telegram.service.param.PreCheckoutQueryAnswer;
import me.nizheg.telegram.service.param.PromotedChatMember;
import me.nizheg.telegram.service.param.RestrictedChatMember;
import me.nizheg.telegram.service.param.ShippingQueryAnswer;
import me.nizheg.telegram.service.param.Sticker;
import me.nizheg.telegram.service.param.StoppingLiveLocation;
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
    public String getToken() {
        return telegramApiClient.getToken();
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
    public StickerSetResponse getStickerSet(String name) {
        return telegramApiClient.getStickerSet(name);
    }

    @Override
    public FileResponse uploadStickerFile(Long userId, InputFile pngSticker) {
        return telegramApiClient.uploadStickerFile(userId, pngSticker);
    }

    @Override
    public BooleanResponse createNewStickerSet(NewStickerSet stickerSet) {
        return telegramApiClient.createNewStickerSet(stickerSet);
    }

    @Override
    public BooleanResponse addStickerToSet(AddingToSetSticker addingToSetSticker) {
        return telegramApiClient.addStickerToSet(addingToSetSticker);
    }

    @Override
    public BooleanResponse setStickerPositionInSet(String sticker, Integer position) {
        return telegramApiClient.setStickerPositionInSet(sticker, position);
    }

    @Override
    public BooleanResponse deleteStickerFromSet(String sticker) {
        return telegramApiClient.deleteStickerFromSet(sticker);
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
    public BooleanResponse kickChatMember(KickedChatMember kickedChatMember) {
        return telegramApiClient.kickChatMember(kickedChatMember);
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
    public BooleanResponse restrictChatMember(RestrictedChatMember restrictedChatMember) {
        try {
            return telegramApiClient.restrictChatMember(restrictedChatMember);
        } catch (TelegramApiException ex) {
            storeFail(ex, restrictedChatMember.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, restrictedChatMember.getChatId());
            throw ex;
        }
    }

    @Override
    public BooleanResponse promoteChatMember(PromotedChatMember promotedChatMember) {
        try {
            return telegramApiClient.promoteChatMember(promotedChatMember);
        } catch (TelegramApiException ex) {
            storeFail(ex, promotedChatMember.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, promotedChatMember.getChatId());
            throw ex;
        }
    }

    @Override
    public StringResponse exportChatInviteLink(ChatId chatId) {
        try {
            return telegramApiClient.exportChatInviteLink(chatId);
        } catch (TelegramApiException ex) {
            storeFail(ex, chatId);
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, chatId);
            throw ex;
        }
    }

    @Override
    public BooleanResponse setChatPhoto(ChatId chatId, InputFile photo) {
        try {
            return telegramApiClient.setChatPhoto(chatId, photo);
        } catch (TelegramApiException ex) {
            storeFail(ex, chatId);
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, chatId);
            throw ex;
        }
    }

    @Override
    public BooleanResponse deleteChatPhoto(ChatId chatId) {
        try {
            return telegramApiClient.deleteChatPhoto(chatId);
        } catch (TelegramApiException ex) {
            storeFail(ex, chatId);
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, chatId);
            throw ex;
        }
    }

    @Override
    public BooleanResponse setChatTitle(ChatId chatId, String title) {
        try {
            return telegramApiClient.setChatTitle(chatId, title);
        } catch (TelegramApiException ex) {
            storeFail(ex, chatId);
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, chatId);
            throw ex;
        }
    }

    @Override
    public BooleanResponse setChatDescription(ChatId chatId, String description) {
        try {
            return telegramApiClient.setChatDescription(chatId, description);
        } catch (TelegramApiException ex) {
            storeFail(ex, chatId);
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, chatId);
            throw ex;
        }
    }

    @Override
    public BooleanResponse pinChatMessage(PinnedChatMessage pinnedChatMessage) {
        try {
            return telegramApiClient.pinChatMessage(pinnedChatMessage);
        } catch (TelegramApiException ex) {
            storeFail(ex, pinnedChatMessage.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, pinnedChatMessage.getChatId());
            throw ex;
        }
    }

    @Override
    public BooleanResponse unpinChatMessage(ChatId chatId) {
        try {
            return telegramApiClient.unpinChatMessage(chatId);
        } catch (TelegramApiException ex) {
            storeFail(ex, chatId);
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, chatId);
            throw ex;
        }
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
    public BooleanResponse setChatStickerSet(ChatId chatId, String stickerSetName) {
        try {
            return telegramApiClient.setChatStickerSet(chatId, stickerSetName);
        } catch (TelegramApiException ex) {
            storeFail(ex, chatId);
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, chatId);
            throw ex;
        }
    }

    @Override
    public BooleanResponse deleteChatStickerSet(ChatId chatId) {
        try {
            return telegramApiClient.deleteChatStickerSet(chatId);
        } catch (TelegramApiException ex) {
            storeFail(ex, chatId);
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, chatId);
            throw ex;
        }
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
    public MessageResponse sendInvoice(Invoice invoice) {
        try {
            return telegramApiClient.sendInvoice(invoice);
        } catch (TelegramApiException ex) {
            storeFail(ex, new ChatId(invoice.getChatId()));
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, new ChatId(invoice.getChatId()));
            throw ex;
        }
    }

    @Override
    public BooleanResponse answerShippingQuery(ShippingQueryAnswer shippingQueryAnswer) {
        return telegramApiClient.answerShippingQuery(shippingQueryAnswer);
    }

    @Override
    public BooleanResponse answerPreCheckoutQuery(PreCheckoutQueryAnswer preCheckoutQueryAnswer) {
        return telegramApiClient.answerPreCheckoutQuery(preCheckoutQueryAnswer);
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
    public MessageOrBooleanResponse editMessageLiveLocation(EditedLiveLocation editedLiveLocation) {
        try {
            return telegramApiClient.editMessageLiveLocation(editedLiveLocation);
        } catch (TelegramApiException ex) {
            storeFail(ex, editedLiveLocation.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, editedLiveLocation.getChatId());
            throw ex;
        }
    }

    @Override
    public MessageOrBooleanResponse stopMessageLiveLocation(StoppingLiveLocation stoppingLiveLocation) {
        try {
            return telegramApiClient.stopMessageLiveLocation(stoppingLiveLocation);
        } catch (TelegramApiException ex) {
            storeFail(ex, stoppingLiveLocation.getChatId());
            throw ex;
        } catch (RuntimeException ex) {
            storeFail(ex, stoppingLiveLocation.getChatId());
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

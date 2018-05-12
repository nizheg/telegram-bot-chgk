package me.nizheg.telegram.bot.chgk.telegram;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.api.model.AtomicResponse;
import me.nizheg.telegram.bot.api.model.Chat;
import me.nizheg.telegram.bot.api.model.ChatAction;
import me.nizheg.telegram.bot.api.model.ChatMember;
import me.nizheg.telegram.bot.api.model.CollectionResponse;
import me.nizheg.telegram.bot.api.model.File;
import me.nizheg.telegram.bot.api.model.GameHighScore;
import me.nizheg.telegram.bot.api.model.MessageOrBooleanResponse;
import me.nizheg.telegram.bot.api.model.StickerSet;
import me.nizheg.telegram.bot.api.model.Update;
import me.nizheg.telegram.bot.api.model.UpdateType;
import me.nizheg.telegram.bot.api.model.User;
import me.nizheg.telegram.bot.api.model.UserProfilePhotos;
import me.nizheg.telegram.bot.api.model.WebhookInfo;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.AddingToSetSticker;
import me.nizheg.telegram.bot.api.service.param.AnswerCallbackRequest;
import me.nizheg.telegram.bot.api.service.param.Audio;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.Contact;
import me.nizheg.telegram.bot.api.service.param.Document;
import me.nizheg.telegram.bot.api.service.param.EditedLiveLocation;
import me.nizheg.telegram.bot.api.service.param.EditedMessage;
import me.nizheg.telegram.bot.api.service.param.ForwardingMessage;
import me.nizheg.telegram.bot.api.service.param.Game;
import me.nizheg.telegram.bot.api.service.param.GameHighScoreRequest;
import me.nizheg.telegram.bot.api.service.param.GameScore;
import me.nizheg.telegram.bot.api.service.param.InlineAnswer;
import me.nizheg.telegram.bot.api.service.param.InputFile;
import me.nizheg.telegram.bot.api.service.param.Invoice;
import me.nizheg.telegram.bot.api.service.param.KickedChatMember;
import me.nizheg.telegram.bot.api.service.param.Location;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.api.service.param.NewStickerSet;
import me.nizheg.telegram.bot.api.service.param.Photo;
import me.nizheg.telegram.bot.api.service.param.PinnedChatMessage;
import me.nizheg.telegram.bot.api.service.param.PreCheckoutQueryAnswer;
import me.nizheg.telegram.bot.api.service.param.PromotedChatMember;
import me.nizheg.telegram.bot.api.service.param.RestrictedChatMember;
import me.nizheg.telegram.bot.api.service.param.ShippingQueryAnswer;
import me.nizheg.telegram.bot.api.service.param.Sticker;
import me.nizheg.telegram.bot.api.service.param.StoppingLiveLocation;
import me.nizheg.telegram.bot.api.service.param.UserProfilePhotosRequest;
import me.nizheg.telegram.bot.api.service.param.Venue;
import me.nizheg.telegram.bot.api.service.param.Video;
import me.nizheg.telegram.bot.api.service.param.VideoNote;
import me.nizheg.telegram.bot.api.service.param.Voice;
import me.nizheg.telegram.bot.chgk.dto.ChatError;
import me.nizheg.telegram.bot.chgk.service.ChatService;

/**
 * @author Nikolay Zhegalin
 */
public class TelegramApiClientWrapper implements TelegramApiClient {

    private final TelegramApiClient telegramApiClient;
    private final ChatService chatService;
    private final Log logger = LogFactory.getLog(getClass());

    public TelegramApiClientWrapper(
            @Nonnull TelegramApiClient telegramApiClient,
            @Nonnull ChatService chatService) {
        Validate.notNull(telegramApiClient);
        Validate.notNull(chatService);
        this.chatService = chatService;
        this.telegramApiClient = telegramApiClient;
    }

    @Override
    public String getToken() {
        return telegramApiClient.getToken();
    }

    @Override
    public AtomicResponse<User> getMe() {
        return telegramApiClient.getMe();
    }

    @Override
    public AtomicResponse<WebhookInfo> getWebhookInfo() {
        return telegramApiClient.getWebhookInfo();
    }

    @Override
    public AtomicResponse<me.nizheg.telegram.bot.api.model.Message> sendMessage(Message message) {
        try {
            return telegramApiClient.sendMessage(message);
        } catch (RuntimeException ex) {
            storeFail(ex, message.getChatId());
            throw ex;
        }
    }

    @Override
    public AtomicResponse<me.nizheg.telegram.bot.api.model.Message> forwardMessage(ForwardingMessage message) {
        try {
            return telegramApiClient.forwardMessage(message);
        } catch (RuntimeException ex) {
            storeFail(ex, message.getChatId());
            throw ex;
        }
    }

    @Override
    public AtomicResponse<me.nizheg.telegram.bot.api.model.Message> sendPhoto(Photo photo) {
        try {
            return telegramApiClient.sendPhoto(photo);
        } catch (RuntimeException ex) {
            storeFail(ex, photo.getChatId());
            throw ex;
        }
    }

    @Override
    public AtomicResponse<me.nizheg.telegram.bot.api.model.Message> sendAudio(Audio audio) {
        try {
            return telegramApiClient.sendAudio(audio);
        } catch (RuntimeException ex) {
            storeFail(ex, audio.getChatId());
            throw ex;
        }
    }

    @Override
    public AtomicResponse<me.nizheg.telegram.bot.api.model.Message> sendDocument(Document document) {
        try {
            return telegramApiClient.sendDocument(document);
        } catch (RuntimeException ex) {
            storeFail(ex, document.getChatId());
            throw ex;
        }
    }

    @Override
    public AtomicResponse<me.nizheg.telegram.bot.api.model.Message> sendSticker(Sticker sticker) {
        try {
            return telegramApiClient.sendSticker(sticker);
        } catch (RuntimeException ex) {
            storeFail(ex, sticker.getChatId());
            throw ex;
        }
    }

    @Override
    public AtomicResponse<StickerSet> getStickerSet(String name) {
        return telegramApiClient.getStickerSet(name);
    }

    @Override
    public AtomicResponse<File> uploadStickerFile(Long userId, InputFile pngSticker) {
        return telegramApiClient.uploadStickerFile(userId, pngSticker);
    }

    @Override
    public AtomicResponse<Boolean> createNewStickerSet(NewStickerSet stickerSet) {
        return telegramApiClient.createNewStickerSet(stickerSet);
    }

    @Override
    public AtomicResponse<Boolean> addStickerToSet(AddingToSetSticker addingToSetSticker) {
        return telegramApiClient.addStickerToSet(addingToSetSticker);
    }

    @Override
    public AtomicResponse<Boolean> setStickerPositionInSet(String sticker, Integer position) {
        return telegramApiClient.setStickerPositionInSet(sticker, position);
    }

    @Override
    public AtomicResponse<Boolean> deleteStickerFromSet(String sticker) {
        return telegramApiClient.deleteStickerFromSet(sticker);
    }

    @Override
    public AtomicResponse<me.nizheg.telegram.bot.api.model.Message> sendVideo(Video video) {
        try {
            return telegramApiClient.sendVideo(video);
        } catch (RuntimeException ex) {
            storeFail(ex, video.getChatId());
            throw ex;
        }
    }

    @Override
    public AtomicResponse<me.nizheg.telegram.bot.api.model.Message> sendVoice(Voice voice) {
        try {
            return telegramApiClient.sendVoice(voice);
        } catch (RuntimeException ex) {
            storeFail(ex, voice.getChatId());
            throw ex;
        }
    }

    @Override
    public AtomicResponse<me.nizheg.telegram.bot.api.model.Message> sendVideoNote(VideoNote videoNote) {
        try {
            return telegramApiClient.sendVideoNote(videoNote);
        } catch (RuntimeException ex) {
            storeFail(ex, videoNote.getChatId());
            throw ex;
        }
    }

    @Override
    public AtomicResponse<Boolean> sendChatAction(ChatAction chatAction, ChatId chatId) {
        return telegramApiClient.sendChatAction(chatAction, chatId);
    }

    @Override
    public AtomicResponse<UserProfilePhotos> getUserProfilePhotos(UserProfilePhotosRequest userProfilePhotosRequest) {
        return telegramApiClient.getUserProfilePhotos(userProfilePhotosRequest);
    }

    @Override
    public AtomicResponse<File> getFile(String fileId) {
        return telegramApiClient.getFile(fileId);
    }

    @Override
    public AtomicResponse<Boolean> kickChatMember(KickedChatMember kickedChatMember) {
        return telegramApiClient.kickChatMember(kickedChatMember);
    }

    @Override
    public AtomicResponse<Boolean> leaveChat(ChatId chatId) {
        return telegramApiClient.leaveChat(chatId);
    }

    @Override
    public AtomicResponse<Boolean> unbanChatMember(ChatId chatId, Long userId) {
        return telegramApiClient.unbanChatMember(chatId, userId);
    }

    @Override
    public AtomicResponse<Boolean> restrictChatMember(RestrictedChatMember restrictedChatMember) {
        try {
            return telegramApiClient.restrictChatMember(restrictedChatMember);
        } catch (RuntimeException ex) {
            storeFail(ex, restrictedChatMember.getChatId());
            throw ex;
        }
    }

    @Override
    public AtomicResponse<Boolean> promoteChatMember(PromotedChatMember promotedChatMember) {
        try {
            return telegramApiClient.promoteChatMember(promotedChatMember);
        } catch (RuntimeException ex) {
            storeFail(ex, promotedChatMember.getChatId());
            throw ex;
        }
    }

    @Override
    public AtomicResponse<String> exportChatInviteLink(ChatId chatId) {
        try {
            return telegramApiClient.exportChatInviteLink(chatId);
        } catch (RuntimeException ex) {
            storeFail(ex, chatId);
            throw ex;
        }
    }

    @Override
    public AtomicResponse<Boolean> setChatPhoto(ChatId chatId, InputFile photo) {
        try {
            return telegramApiClient.setChatPhoto(chatId, photo);
        } catch (RuntimeException ex) {
            storeFail(ex, chatId);
            throw ex;
        }
    }

    @Override
    public AtomicResponse<Boolean> deleteChatPhoto(ChatId chatId) {
        try {
            return telegramApiClient.deleteChatPhoto(chatId);
        } catch (RuntimeException ex) {
            storeFail(ex, chatId);
            throw ex;
        }
    }

    @Override
    public AtomicResponse<Boolean> setChatTitle(ChatId chatId, String title) {
        try {
            return telegramApiClient.setChatTitle(chatId, title);
        } catch (RuntimeException ex) {
            storeFail(ex, chatId);
            throw ex;
        }
    }

    @Override
    public AtomicResponse<Boolean> setChatDescription(ChatId chatId, String description) {
        try {
            return telegramApiClient.setChatDescription(chatId, description);
        } catch (RuntimeException ex) {
            storeFail(ex, chatId);
            throw ex;
        }
    }

    @Override
    public AtomicResponse<Boolean> pinChatMessage(PinnedChatMessage pinnedChatMessage) {
        try {
            return telegramApiClient.pinChatMessage(pinnedChatMessage);
        } catch (RuntimeException ex) {
            storeFail(ex, pinnedChatMessage.getChatId());
            throw ex;
        }
    }

    @Override
    public AtomicResponse<Boolean> unpinChatMessage(ChatId chatId) {
        try {
            return telegramApiClient.unpinChatMessage(chatId);
        } catch (RuntimeException ex) {
            storeFail(ex, chatId);
            throw ex;
        }
    }

    @Override
    public AtomicResponse<Chat> getChat(ChatId chatId) {
        return telegramApiClient.getChat(chatId);
    }

    @Override
    public CollectionResponse<ChatMember> getChatAdministrators(ChatId chatId) {
        return telegramApiClient.getChatAdministrators(chatId);
    }

    @Override
    public AtomicResponse<Integer> getChatMembersCount(ChatId chatId) {
        return telegramApiClient.getChatMembersCount(chatId);
    }

    @Override
    public AtomicResponse<ChatMember> getChatMember(ChatId chatId, Long userId) {
        return telegramApiClient.getChatMember(chatId, userId);
    }

    @Override
    public AtomicResponse<Boolean> setChatStickerSet(ChatId chatId, String stickerSetName) {
        try {
            return telegramApiClient.setChatStickerSet(chatId, stickerSetName);
        } catch (RuntimeException ex) {
            storeFail(ex, chatId);
            throw ex;
        }
    }

    @Override
    public AtomicResponse<Boolean> deleteChatStickerSet(ChatId chatId) {
        try {
            return telegramApiClient.deleteChatStickerSet(chatId);
        } catch (RuntimeException ex) {
            storeFail(ex, chatId);
            throw ex;
        }
    }

    @Override
    public AtomicResponse<Boolean> answerCallbackQuery(AnswerCallbackRequest answerCallbackRequest) {
        return telegramApiClient.answerCallbackQuery(answerCallbackRequest);
    }

    @Override
    public CollectionResponse<Update> getUpdates(
            Long offset,
            Integer limit,
            Integer timeout,
            List<UpdateType> allowedUpdates) {
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
    public AtomicResponse<Boolean> deleteMessage(ChatId chatId, Long messageId) {
        return telegramApiClient.deleteMessage(chatId, messageId);
    }

    @Override
    public AtomicResponse<Boolean> answerInlineQuery(InlineAnswer inlineAnswer) {
        return telegramApiClient.answerInlineQuery(inlineAnswer);
    }

    @Override
    public AtomicResponse<me.nizheg.telegram.bot.api.model.Message> sendGame(Game game) {
        try {
            return telegramApiClient.sendGame(game);
        } catch (RuntimeException ex) {
            storeFail(ex, game.getChatId());
            throw ex;
        }
    }

    @Override
    public MessageOrBooleanResponse setGameScore(GameScore gameScore) {
        try {
            return telegramApiClient.setGameScore(gameScore);
        } catch (RuntimeException ex) {
            storeFail(ex, gameScore.getChatId());
            throw ex;
        }
    }

    @Override
    public CollectionResponse<GameHighScore> getGameHighScores(GameHighScoreRequest gameHighScoreRequest) {
        try {
            return telegramApiClient.getGameHighScores(gameHighScoreRequest);
        } catch (RuntimeException ex) {
            storeFail(ex, gameHighScoreRequest.getChatId());
            throw ex;
        }
    }

    @Override
    public AtomicResponse<me.nizheg.telegram.bot.api.model.Message> sendInvoice(Invoice invoice) {
        try {
            return telegramApiClient.sendInvoice(invoice);
        } catch (RuntimeException ex) {
            storeFail(ex, new ChatId(invoice.getChatId()));
            throw ex;
        }
    }

    @Override
    public AtomicResponse<Boolean> answerShippingQuery(ShippingQueryAnswer shippingQueryAnswer) {
        return telegramApiClient.answerShippingQuery(shippingQueryAnswer);
    }

    @Override
    public AtomicResponse<Boolean> answerPreCheckoutQuery(PreCheckoutQueryAnswer preCheckoutQueryAnswer) {
        return telegramApiClient.answerPreCheckoutQuery(preCheckoutQueryAnswer);
    }

    @Override
    public AtomicResponse<me.nizheg.telegram.bot.api.model.Message> sendLocation(Location location) {
        try {
            return telegramApiClient.sendLocation(location);
        } catch (RuntimeException ex) {
            storeFail(ex, location.getChatId());
            throw ex;
        }
    }

    @Override
    public MessageOrBooleanResponse editMessageLiveLocation(EditedLiveLocation editedLiveLocation) {
        try {
            return telegramApiClient.editMessageLiveLocation(editedLiveLocation);
        } catch (RuntimeException ex) {
            storeFail(ex, editedLiveLocation.getChatId());
            throw ex;
        }
    }

    @Override
    public MessageOrBooleanResponse stopMessageLiveLocation(StoppingLiveLocation stoppingLiveLocation) {
        try {
            return telegramApiClient.stopMessageLiveLocation(stoppingLiveLocation);
        } catch (RuntimeException ex) {
            storeFail(ex, stoppingLiveLocation.getChatId());
            throw ex;
        }
    }

    @Override
    public AtomicResponse<me.nizheg.telegram.bot.api.model.Message> sendVenue(Venue venue) {
        try {
            return telegramApiClient.sendVenue(venue);
        } catch (RuntimeException ex) {
            storeFail(ex, venue.getChatId());
            throw ex;
        }
    }

    @Override
    public AtomicResponse<me.nizheg.telegram.bot.api.model.Message> sendContact(Contact contact) {
        try {
            return telegramApiClient.sendContact(contact);
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
            logger.error("Unable to store error: [" + chatError.getChatId() + ", " + chatError.getCode() + ", "
                    + chatError.getDescription() + "]", e);
        }
    }
}

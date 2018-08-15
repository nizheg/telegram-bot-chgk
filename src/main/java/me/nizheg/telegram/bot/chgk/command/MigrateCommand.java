package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.nizheg.telegram.bot.api.model.ChatAction;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.exception.CipherException;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.service.Cipher;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.util.TelegramApiUtil;

@UserInChannel
public class MigrateCommand extends ChatCommand {

    private final ChatService chatService;
    private final ChatGameService chatGameService;
    private final Cipher cipher;

    public MigrateCommand(
            @Nonnull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @Nonnull ChatService chatService,
            @Nonnull ChatGameService chatGameService,
            @Nonnull Cipher cipher) {
        super(telegramApiClientSupplier);
        this.chatService = chatService;
        this.chatGameService = chatGameService;
        this.cipher = cipher;
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        Long chatId = ctx.getChatId();
        if (StringUtils.isBlank(ctx.getText())) {
            String chatIdEncrypted;
            try {
                chatIdEncrypted = cipher.encrypt(String.valueOf(chatId));
            } catch (CipherException e) {
                throw new IllegalStateException(e);
            }
            getTelegramApiClient().sendMessage(new Message(
                    "<i>Для переноса статистики использования в другой чат нажмите кнопку и выберите необходимый чат.</i>"
                            + "\n\n<b>Внимание! Вся статистика использования в выбранном чате перед обновлением будет очищена.</b>",
                    chatId, ParseMode.HTML, null, null,
                    TelegramApiUtil.createInlineMarkupForPrintTextInAnotherChat("Выбрать чат",
                            "/migrate " + chatIdEncrypted)));
        } else {
            Long fromChatId;
            try {
                fromChatId = Long.valueOf(cipher.decrypt(ctx.getText()));
            } catch (CipherException | RuntimeException e) {
                throw new CommandException("Некорректный параметр. Вернитесь в чат, откуда вы "
                        + "желаете перенести статистику, и воспользуйтесь в нём командой /migrate", e);
            }
            getTelegramApiClient().sendChatAction(ChatAction.TYPING, new ChatId(chatId));
            chatGameService.stopChatGame(ctx.getChatId());
            chatService.deactivateChat(ctx.getChatId());
            chatService.migrateChatToAnother(fromChatId, chatId);
            getTelegramApiClient().sendMessage(
                    new Message("<i>Перенос успешно завершен</i>", chatId, ParseMode.HTML));

        }

    }

    @Override
    public String getCommandName() {
        return "migrate";
    }

    @Nullable
    @Override
    public String getDescription() {
        return "/migrate - перенести статистику в другой чат";
    }
}

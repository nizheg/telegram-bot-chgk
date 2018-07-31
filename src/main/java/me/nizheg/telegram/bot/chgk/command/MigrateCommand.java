package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.nizheg.telegram.bot.api.model.ChatAction;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.util.TelegramApiUtil;

public class MigrateCommand extends ChatCommand {

    private final static Pattern PATTERN_MIGRATE_MODE = Pattern.compile("(?<chatId>-?[0-9]+)");
    private final ChatService chatService;
    private final ChatGameService chatGameService;

    public MigrateCommand(
            @Nonnull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @Nonnull ChatService chatService, ChatGameService chatGameService) {
        super(telegramApiClientSupplier);
        this.chatService = chatService;
        this.chatGameService = chatGameService;
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        Long chatId = ctx.getChatId();
        if (StringUtils.isBlank(ctx.getText())) {
            getTelegramApiClient().sendMessage(new Message(
                    "<i>Для переноса статистики использования в другой чат нажмите кнопку и выберите необходимый чат.</i>"
                            + "\n\n<b>Внимание! Вся статистика использования в выбранном чате перед обновлением будет очищена.</b>",
                    chatId, ParseMode.HTML, null, null,
                    TelegramApiUtil.createInlineMarkupForPrintTextInAnotherChat("Выбрать чат", "/migrate " + chatId)));
        } else {
            Matcher migrateModeMatcher = PATTERN_MIGRATE_MODE.matcher(ctx.getText());
            if (migrateModeMatcher.matches()) {
                Long fromChatId = Long.valueOf(migrateModeMatcher.group("chatId"));
                getTelegramApiClient().sendChatAction(ChatAction.TYPING, new ChatId(chatId));
                chatGameService.stopChatGame(ctx.getChatId());
                chatService.deactivateChat(ctx.getChatId());
                chatService.migrateChatToAnother(fromChatId, chatId);
                getTelegramApiClient().sendMessage(
                        new Message("<i>Перенос успешно завершен</i>", chatId, ParseMode.HTML));
            }
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

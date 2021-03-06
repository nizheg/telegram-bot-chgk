package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.model.ChatMemberStatus;
import me.nizheg.telegram.bot.api.model.KeyboardButton;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.model.ReplyKeyboardMarkup;
import me.nizheg.telegram.bot.api.model.ReplyKeyboardRemove;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.dto.Category;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.UsageStat;
import me.nizheg.telegram.bot.chgk.dto.composite.Tournament;
import me.nizheg.telegram.bot.chgk.service.CategoryService;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.service.TaskService;
import me.nizheg.telegram.bot.chgk.util.TourList;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.starter.service.preconditions.Permission;
import me.nizheg.telegram.util.Emoji;

/**
 * @author Nikolay Zhegalin
 */
@ChatActive
@Permission(chatMemberStatuses = {ChatMemberStatus.ADMINISTRATOR, ChatMemberStatus.CREATOR},
        failOnUnsatisfied = true,
        description = "Только администраторы имеют право менять категорию")
@Component
public class CategoryCommand extends ChatCommand {

    private static final String COMMAND_NAME = "category";
    private static final String SHORT_COMMAND_NAME = Emoji.BOOKS;
    private static final String OPTION_FORMAT = "\\s*([a-zA-z0-9_]+)\\s*";
    private static final Pattern OPTION_PATTERN = Pattern.compile(OPTION_FORMAT);

    private final CategoryService categoryService;
    private final ChatService chatService;
    private final ChatGameService chatGameService;
    private final TaskService taskService;
    private final TourList tourList;
    private volatile List<Category> categories = new ArrayList<>();

    public CategoryCommand(
            @NonNull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @NonNull CategoryService categoryService,
            @NonNull ChatService chatService,
            @NonNull ChatGameService chatGameService,
            @NonNull TaskService taskService,
            @NonNull TourList tourList) {
        super(telegramApiClientSupplier);
        this.categoryService = categoryService;
        this.chatService = chatService;
        this.chatGameService = chatGameService;
        this.taskService = taskService;
        this.tourList = tourList;
    }

    @Override
    public int getPriority() {
        return 20;
    }

    protected ChatService getChatService() {
        return chatService;
    }

    protected ChatGameService getChatGameService() {
        return chatGameService;
    }

    @Override
    public void execute(CommandContext ctx) {
        String options = StringUtils.defaultString(ctx.getText());
        Matcher matcher = OPTION_PATTERN.matcher(options);
        String categoryId;
        if (matcher.matches()) {
            categoryId = matcher.group(1);
        } else {
            categoryId = resolveCategoryId(ctx.getText());
        }
        if (categoryId != null) {
            ChatGame chatGame = chatGameService.getGame(new Chat(ctx.getChat()));
            if (categoryId.equals(Category.CURRENT)) {
                Category currentCategory = chatGame.getCategory();
                sendCurrentCategory(ctx, chatGame, currentCategory);
            } else if (categoryService.isExists(categoryId)) {
                Category category = chatGame.setCategory(categoryId);
                sendCategorySelectedMessage(ctx, category);
                if (isTourCategory(category)) {
                    sendTournamentsList(ctx);
                }
            }
        } else {
            this.categories = categoryService.getCollection();
            sendCategories(ctx, this.categories);
        }
    }

    @Override
    public void sendCallbackResponse(CommandContext ctx) {
    }

    private String resolveCategoryId(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        for (Category category : this.categories) {
            if (category.getId().equals(text) || category.getName().equalsIgnoreCase(text)) {
                return category.getId();
            }
        }
        return null;
    }

    private void sendTournamentsList(CommandContext ctx) {
        Message tournamentsList = tourList.getTournamentsListOfChat(ctx.getChatId(), 0);
        TelegramApiClient telegramApiClient = getTelegramApiClient();
        telegramApiClient.sendMessage(tournamentsList)
                .setCallback(new CallbackRequestDefaultCallback<>(ctx, telegramApiClient));
    }

    private void sendCategories(CommandContext ctx, List<Category> categories) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setSelective(false);
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<List<KeyboardButton>> keyboard = new ArrayList<>();
        for (Category category : categories) {
            keyboard.add(Collections.singletonList(new KeyboardButton(SHORT_COMMAND_NAME + " " + category.getName())));
        }
        replyKeyboardMarkup.setKeyboard(keyboard);
        TelegramApiClient telegramApiClient = getTelegramApiClient();
        telegramApiClient.sendMessage(
                Message.safeMessageBuilder()
                        .text("<i>Выберите категорию вопросов</i>")
                        .chatId(new ChatId(ctx.getChatId()))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(false)
                        .replyMarkup(replyKeyboardMarkup)
                        .build())
                .setCallback(new CallbackRequestDefaultCallback<>(ctx, telegramApiClient));
    }

    private void sendCurrentCategory(CommandContext ctx, ChatGame chatGame, Category currentCategory) {
        String categoryName;
        UsageStat stat = null;
        long chatId = chatGame.getChatId();
        if (isTourCategory(currentCategory)) {
            Optional<Tournament> currentTournamentOptional = chatGame.getTournament();
            if (currentTournamentOptional.isPresent()) {
                Tournament currentTournament = currentTournamentOptional.get();
                categoryName = currentTournament.getTitle();
                stat = taskService.getUsageStatForChatByTournament(chatId, currentTournament);
            } else {
                categoryName = currentCategory.getName();
            }
        } else {
            categoryName = currentCategory.getName();
            stat = taskService.getUsageStatForChat(chatId, currentCategory);
        }

        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setSelective(false);
        replyKeyboardRemove.setRemoveKeyboard(true);
        StringBuilder messageBuilder = new StringBuilder().append("<i>Ваша текущая категория:</i> <b>")
                .append(categoryName)
                .append("</b>");
        if (stat != null) {
            messageBuilder.append("\n<i>Использовано вопросов:</i> <b>")
                    .append(stat.getUsedCount())
                    .append(" из ")
                    .append(stat.getCount())
                    .append("</b>");
        }
        TelegramApiClient telegramApiClient = getTelegramApiClient();
        telegramApiClient.sendMessage(
                Message.safeMessageBuilder()
                        .text(messageBuilder.toString())
                        .chatId(new ChatId(chatId))
                        .parseMode(ParseMode.HTML)
                        .replyMarkup(replyKeyboardRemove)
                        .build())
                .setCallback(new CallbackRequestDefaultCallback<>(ctx, telegramApiClient));
    }

    private boolean isTourCategory(Category currentCategory) {
        return Category.TOUR.equals(currentCategory.getId());
    }

    private void sendCategorySelectedMessage(CommandContext ctx, Category category) {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setSelective(false);
        replyKeyboardRemove.setRemoveKeyboard(true);
        TelegramApiClient telegramApiClient = getTelegramApiClient();
        telegramApiClient.sendMessage(
                Message.safeMessageBuilder()
                        .text("<i>Выбрана новая категория</i> <b>" + category.getName() + "</b>")
                        .chatId(new ChatId(ctx.getChatId()))
                        .parseMode(ParseMode.HTML)
                        .replyMarkup(replyKeyboardRemove)
                        .build())
                .setCallback(new CallbackRequestDefaultCallback<>(ctx, telegramApiClient));
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME + "|" + SHORT_COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return "/category - выбрать категорию вопросов";
    }
}

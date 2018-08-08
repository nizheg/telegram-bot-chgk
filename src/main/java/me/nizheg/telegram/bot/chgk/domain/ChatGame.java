package me.nizheg.telegram.bot.chgk.domain;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.api.model.User;
import me.nizheg.telegram.bot.chgk.dto.Answer;
import me.nizheg.telegram.bot.chgk.dto.AnswerLog;
import me.nizheg.telegram.bot.chgk.dto.Category;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.LightTask;
import me.nizheg.telegram.bot.chgk.dto.LightTour;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.dto.composite.Tournament;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;
import me.nizheg.telegram.bot.chgk.exception.GameException;
import me.nizheg.telegram.bot.chgk.exception.IllegalIdException;
import me.nizheg.telegram.bot.chgk.exception.TooOftenCallingException;
import me.nizheg.telegram.bot.chgk.exception.TournamentIsNotSelectedException;
import me.nizheg.telegram.bot.chgk.service.AnswerLogService;
import me.nizheg.telegram.bot.chgk.service.CategoryService;
import me.nizheg.telegram.bot.chgk.service.Properties;
import me.nizheg.telegram.bot.chgk.service.TaskService;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;
import me.nizheg.telegram.bot.chgk.service.TourService;
import me.nizheg.telegram.bot.chgk.util.BotInfo;
import me.nizheg.telegram.bot.service.PropertyService;

/**
 * @author Nikolay Zhegalin
 */
@RequiredArgsConstructor
public class ChatGame {

    private final static long NULL_TASK_ID = -1;
    private final static LevenshteinDistance LEVENSHTEIN_DISTANCE = LevenshteinDistance.getDefaultInstance();

    @NonNull
    protected final Chat chat;
    @NonNull
    private final PropertyService propertyService;
    @NonNull
    private final CategoryService categoryService;
    @NonNull
    private final TourService tourService;
    @NonNull
    private final TaskService taskService;
    @NonNull
    private final AnswerLogService answerLogService;
    @NonNull
    private final TelegramUserService telegramUserService;
    @NonNull
    private final BotInfo botInfo;
    @NonNull
    private final Clock clock;
    @Nullable
    private Task currentTask;
    @Nullable
    private OffsetDateTime currentTaskUsageTime;
    private Category category;
    @Nullable
    private Tournament currentTournament;

    @PostConstruct
    public synchronized void init() {
        long chatId = getChatId();
        LightTask lightTask = taskService.getLastUsedTask(chatId);
        if (lightTask == null || NULL_TASK_ID == lightTask.getId()) {
            this.currentTask = null;
        } else {
            this.currentTask = taskService.createCompositeTask(lightTask);
        }
        if (this.currentTask != null) {
            this.currentTaskUsageTime = taskService.getUsageTime(this.currentTask.getId(), chatId);
        }
        String categoryId = propertyService.getValueForChat(Properties.CATEGORY_KEY, chatId);
        Category category;
        if (categoryId != null) {
            category = categoryService.read(categoryId);
        } else {
            category = categoryService.read(Category.ALL);
        }
        this.category = category;
        Long tourId = propertyService.getLongValueForChat(Properties.TOUR_KEY, chatId);
        if (tourId != null) {
            LightTour compositeTour = tourService.createCompositeTour(tourId);
            if (compositeTour instanceof Tournament) {
                this.currentTournament = (Tournament) compositeTour;
            }
        }
    }

    public long getChatId() {
        return chat.getId();
    }

    public Chat getChat() {
        return chat;
    }

    public synchronized Optional<Task> repeatTask() {
        return Optional.ofNullable(currentTask);
    }

    @NonNull
    public synchronized Category getCategory() {
        return category;
    }

    //TODO: remove
    public synchronized Optional<Tournament> getTournament() {
        return Optional.ofNullable(currentTournament);
    }

    public synchronized Category setCategory(String categoryId) {
        categoryId = categoryId == null ? Category.ALL : categoryId;
        propertyService.setValueForChat(Properties.CATEGORY_KEY, categoryId, getChatId());
        Category category = categoryService.read(categoryId);
        this.category = category;
        if (!Category.TOUR.equals(categoryId)) {
            clearTournament();
        }
        return category;
    }

    public synchronized Task setCurrentTask(@Nullable Long taskId) {
        if (taskId == null) {
            taskId = NULL_TASK_ID;
        }
        long chatId = getChatId();
        taskService.setTaskUsed(taskId, chatId);
        Task compositeTask = null;
        if (taskId != NULL_TASK_ID) {
            compositeTask = taskService.createCompositeTask(taskId);
        }
        this.currentTask = compositeTask;
        if (this.currentTask != null) {
            this.currentTaskUsageTime = taskService.getUsageTime(this.currentTask.getId(), chatId);
        } else {
            this.currentTaskUsageTime = null;
        }
        return compositeTask;
    }

    public synchronized void clearCurrentTask() {
        setCurrentTask(NULL_TASK_ID);
    }

    public synchronized TournamentResult setTournament(Long tourId) throws IllegalIdException {
        TournamentResult.TournamentResultBuilder builder = TournamentResult.builder();
        LightTour compositeTour = tourService.createCompositeTour(tourId);
        if (!(compositeTour instanceof Tournament)) {
            throw new IllegalIdException("Tour with id " + tourId + " is not tournament");
        }
        final Tournament tournament = (Tournament) compositeTour;
        if (!LightTour.Status.PUBLISHED.equals(compositeTour.getStatus())) {
            throw new IllegalIdException("Tour with id " + tourId + " is not published yet");
        }
        propertyService.setValueForChat(Properties.TOUR_KEY, tourId, getChatId());
        this.currentTournament = tournament;
        builder.tournament(tournament);
        setCategory(Category.TOUR);
        builder.isCurrentTaskFromTournament(isCurrentTaskFromTournament(tournament));
        builder.currentTask(this.currentTask);
        return builder.build();
    }

    private boolean isCurrentTaskFromTournament(Tournament tournament) {
        return Optional.ofNullable(this.currentTask)
                .map(LightTask::getTourId)
                .flatMap(currentTaskTourId ->
                        tournament.getChildTours()
                                .stream()
                                .filter(lightTour -> currentTaskTourId.equals(lightTour.getId()))
                                .findAny())
                .isPresent();
    }

    private void clearTournament() {
        propertyService.setValueForChat(Properties.TOUR_KEY, (Long) null, getChatId());
        this.currentTournament = null;
    }

    public synchronized Optional<NextTaskResult> nextTaskIfEquals(long taskId) throws GameException {
        if (Optional.ofNullable(this.currentTask).filter(task -> taskId == task.getId()).isPresent()) {
            return Optional.of(nextTask());
        }
        return Optional.empty();
    }

    public synchronized NextTaskResult nextTask() throws GameException {
        NextTaskResult.NextTaskResultBuilder builder = NextTaskResult.builder();
        if (this.currentTask != null) {
            OffsetDateTime usageTime = getUsageTime();
            if (usageTime != null && Duration.between(usageTime, OffsetDateTime.now(clock)).getSeconds() < 5) {
                throw new TooOftenCallingException("Следующий вопрос можно получить не раньше, чем через 5 с");
            }
            Task unansweredTask = throwUnansweredTask();
            builder.unansweredTask(unansweredTask);
        }
        LightTask nextTask;
        Category category = getCategory();
        boolean isTournament = category != null && Category.TOUR.equals(category.getId());
        builder.isTournament(isTournament);
        if (isTournament) {
            if (this.currentTournament == null) {
                throw new TournamentIsNotSelectedException("Турнир не выбран");
            } else {
                builder.tournament(this.currentTournament);
                nextTask = taskService.getNextTaskInTournament(this.currentTournament, this.currentTask);
                if (nextTask == null) {
                    builder.tournamentStat(
                            answerLogService.getStatForChatUserForTournament(getChatId(), botInfo.getBotUser().getId(),
                                    NextTaskResult.STAT_USERS, this.currentTournament.getId()));
                }
            }
        } else {
            nextTask = taskService.getUnusedByChatTask(getChatId(), category);
        }
        if (nextTask != null) {
            Task task = setCurrentTask(nextTask.getId());
            builder.nextTask(task);
        }
        return builder.build();
    }

    @Nullable
    protected Task throwUnansweredTask() {
        if (this.currentTask != null && isTaskUnanswered()) {
            logAnswerOfUser(botInfo.getBotUser(), this.currentTask.getId());
            return this.currentTask;
        }
        return null;
    }

    protected boolean isTaskUnanswered() {
        return this.currentTask != null && !answerLogService.isExistByTaskAndChat(currentTask.getId(), getChatId());
    }

    @Nullable
    protected synchronized OffsetDateTime getUsageTime() {
        return currentTaskUsageTime;
    }

    @Nonnull
    protected Clock getClock() {
        return clock;
    }

    public synchronized UserAnswerResult userAnswer(UserAnswer userAnswer) {
        String text = userAnswer.getText();
        User user = userAnswer.getUser();
        UserAnswerResult.UserAnswerResultBuilder builder = UserAnswerResult.builder();

        if (this.currentTask != null) {
            long currentTaskId = this.currentTask.getId();
            List<Answer> answers = this.currentTask.getAnswers();
            boolean isCorrect = false;
            String exactAnswer = null;
            for (Answer answer : answers) {
                String normalizedAnswer;
                String normalizedText;
                if (Answer.Type.EXACT_WITH_PUNCTUATION.equals(answer.getType())) {
                    normalizedAnswer = normalizeWithPunctuation(answer.getText());
                    normalizedText = normalizeWithPunctuation(text);
                } else {
                    normalizedAnswer = normalize(answer.getText());
                    normalizedText = normalize(text);
                }

                if (equals(normalizedText, normalizedAnswer)) {
                    exactAnswer = null;
                    isCorrect = true;
                    break;
                } else if (Answer.Type.APPROXIMATE.equals(answer.getType()) && isApproximatelyCorrect(normalizedText,
                        normalizedAnswer)) {
                    exactAnswer = answer.getText();
                    isCorrect = true;
                } else if (Answer.Type.CONTAINS.equals(answer.getType()) && normalizedText.contains(normalizedAnswer)) {
                    exactAnswer = null;
                    isCorrect = true;
                    break;
                }
            }
            builder.isCorrect(isCorrect)
                    .exactAnswer(exactAnswer)
                    .currentTask(this.currentTask);
            if (isCorrect) {
                boolean isUserGetHintFromBot = false;
                User answeredUser = user;
                Long botUserId = botInfo.getBotUser().getId();
                if (!chat.isPrivate()) {
                    AnswerLog userAnswerLog = answerLogService.getByTaskAndChat(currentTaskId, user.getId());
                    if (userAnswerLog != null) {
                        isUserGetHintFromBot = userAnswerLog.getTelegramUserId().equals(botUserId);
                    }
                }
                if (isUserGetHintFromBot) {
                    answeredUser = botInfo.getBotUser();
                }
                AnswerLog answerLog = logAnswerOfUser(answeredUser, currentTaskId);
                long firstAnsweredUserId = answerLog.getTelegramUserId();
                builder.firstAnsweredUser(firstAnsweredUserId);
                builder.usageTime(getUsageTime());
            }
        }
        return builder.build();
    }

    public synchronized HintResult getHintForTask(Chat chat, @Nullable Long taskId) throws TooOftenCallingException {
        HintResult.HintResultBuilder builder = HintResult.builder();
        if (taskId != null &&
                Optional.ofNullable(this.currentTask).filter(task -> taskId.equals(task.getId())).isPresent()) {
            Task requestedTask = taskService.createCompositeTask(taskId);
            builder.task(requestedTask).isTaskCurrent(false);
        } else if (this.currentTask != null) {
            OffsetDateTime usageTime = getUsageTime();
            if (usageTime != null && Duration.between(usageTime, OffsetDateTime.now(clock)).getSeconds() < 5) {
                throw new TooOftenCallingException("Подсказку можно получить не раньше, чем через 5 с");
            }
            builder.task(this.currentTask).isTaskCurrent(true);
        }
        HintResult hintResult = builder.build();
        hintResult.getTask().ifPresent(task -> logBotSendHintToChat(chat, task.getId()));
        return hintResult;
    }

    private AnswerLog logAnswerOfUser(User user, Long taskId) {
        AnswerLog answerLog = new AnswerLog();
        answerLog.setTaskId(taskId);
        answerLog.setChatId(getChatId());
        answerLog.setTelegramUserId(user.getId());
        telegramUserService.createOrUpdate(new TelegramUser(user));
        try {
            answerLog = answerLogService.create(answerLog);
        } catch (DuplicationException ex) {
            answerLog = answerLogService.getByTaskAndChat(taskId, getChatId());
        }
        return answerLog;
    }

    private void logBotSendHintToChat(Chat chat, Long taskId) {
        AnswerLog answerLog = new AnswerLog();
        answerLog.setTaskId(taskId);
        answerLog.setChatId(chat.getId());
        answerLog.setTelegramUserId(botInfo.getBotUser().getId());
        try {
            answerLogService.create(answerLog);
        } catch (DuplicationException ex) {
            // ignore
        }
    }

    private boolean equals(String normalizedText, String normalizedAnswer) {
        return normalizedAnswer.equalsIgnoreCase(normalizedText);
    }

    private String normalize(String answer) {
        return answer.toLowerCase().replaceAll("[ё]", "е").replaceAll("[.,-]|\\s", "");
    }

    private String normalizeWithPunctuation(String answer) {
        return answer.toLowerCase().replaceAll("[ё]", "е").replaceAll("\\s", "");
    }

    private boolean isApproximatelyCorrect(String answer, String expected) {
        if (expected.length() < 4) {
            return equals(answer, expected);
        }
        if (expected.length() < 6) {
            return LEVENSHTEIN_DISTANCE.apply(answer, expected) <= 1;
        }
        return LEVENSHTEIN_DISTANCE.apply(answer, expected) <= 2;
    }

}

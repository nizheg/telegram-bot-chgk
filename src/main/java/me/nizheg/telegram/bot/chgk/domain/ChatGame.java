package me.nizheg.telegram.bot.chgk.domain;

import org.apache.commons.lang3.Validate;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

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
import me.nizheg.telegram.bot.chgk.repository.TaskDao;
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
@Component("chatGame")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ChatGame {

    private final static int SECOND = 1000;
    private final static long NULL_TASK_ID = -1;
    protected final Chat chat;
    private final static LevenshteinDistance LEVENSHTEIN_DISTANCE = LevenshteinDistance.getDefaultInstance();
    private Task currentTask;
    private Category category;
    private Tournament currentTournament;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TourService tourService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private AnswerLogService answerLogService;
    @Autowired
    private BotInfo botInfo;
    @Autowired
    private TelegramUserService telegramUserService;

    public ChatGame(Chat chat) {
        Validate.notNull(chat);
        this.chat = chat;
    }

    @PostConstruct
    public synchronized void init() {
        long chatId = getChatId();
        LightTask lightTask = taskDao.getLastUsedTask(chatId);
        if (lightTask == null || NULL_TASK_ID == lightTask.getId()) {
            this.currentTask = null;
        } else {
            this.currentTask = taskService.createCompositeTask(lightTask);
        }
        String categoryId = propertyService.getValueForChat(Properties.CATEGORY_KEY, chatId);
        Category category;
        if (categoryId != null) {
            category = categoryService.read(categoryId);
        } else {
            category = categoryService.read(Category.ALL);
        }
        setCategory(category);
        Long tourId = propertyService.getLongValueForChat(Properties.TOUR_KEY, chatId);
        if (tourId != null) {
            LightTour compositeTour = tourService.createCompositeTour(tourId);
            if (compositeTour instanceof Tournament) {
                setTournament((Tournament) compositeTour);
            }
        }
    }

    public long getChatId() {
        return chat.getId();
    }

    public Chat getChat() {
        return chat;
    }

    public synchronized Task repeatTask() {
        return currentTask;
    }

    protected synchronized Task getCurrentTask() {
        return currentTask;
    }

    public synchronized boolean isCurrentTaskFromTournament() {
        Task currentTask = getCurrentTask();
        Long currentTaskTourId = currentTask == null ? null : currentTask.getTourId();
        boolean isCurrentTaskFromTournament = false;
        if (currentTaskTourId != null) {
            for (LightTour lightTour : getTournament().getChildTours()) {
                if (currentTaskTourId.equals(lightTour.getId())) {
                    isCurrentTaskFromTournament = true;
                    break;
                }
            }
        }
        return isCurrentTaskFromTournament;
    }

    public synchronized Category getCategory() {
        return category;
    }

    private void setCategory(Category category) {
        this.category = category;
    }

    public synchronized Tournament getTournament() {
        return currentTournament;
    }

    private void setTournament(Tournament currentTournament) {
        this.currentTournament = currentTournament;
    }

    public synchronized Category setCategory(String categoryId) {
        categoryId = categoryId == null ? Category.ALL : categoryId;
        propertyService.setValueForChat(Properties.CATEGORY_KEY, categoryId, getChatId());
        Category category = categoryService.read(categoryId);
        setCategory(category);
        if (!Category.TOUR.equals(categoryId)) {
            clearTournament();
        }
        return category;
    }

    public synchronized Task setCurrentTask(Long taskId) {
        if (taskId == null) {
            taskId = NULL_TASK_ID;
        }
        taskService.setTaskUsed(taskId, getChatId());
        Task compositeTask = null;
        if (taskId != NULL_TASK_ID) {
            compositeTask = taskService.createCompositeTask(taskId);
        }
        this.currentTask = compositeTask;
        return compositeTask;
    }

    public synchronized void clearCurrentTask() {
        setCurrentTask(NULL_TASK_ID);
    }

    public synchronized Tournament setTournament(Long tourId) throws IllegalIdException {
        LightTour compositeTour = tourService.createCompositeTour(tourId);
        if (!(compositeTour instanceof Tournament)) {
            throw new IllegalIdException("Tour with id " + tourId + " is not tournament");
        }
        if (!LightTour.Status.PUBLISHED.equals(compositeTour.getStatus())) {
            throw new IllegalIdException("Tour with id " + tourId + " is not published yet");
        }
        propertyService.setValueForChat(Properties.TOUR_KEY, tourId, getChatId());
        setTournament((Tournament) compositeTour);
        setCategory(Category.TOUR);
        return (Tournament) compositeTour;
    }

    private void clearTournament() {
        propertyService.setValueForChat(Properties.TOUR_KEY, (Long) null, getChatId());
        setTournament((Tournament) null);
    }

    public synchronized NextTaskResult nextTask() throws GameException {
        NextTaskResult result = new NextTaskResult();
        Task currentTask = getCurrentTask();
        if (currentTask != null) {
            Date usageTime = getUsageTime();
            if (usageTime != null && System.currentTimeMillis() - usageTime.getTime() <= 5 * SECOND) {
                throw new TooOftenCallingException("Следующий вопрос можно получить не раньше, чем через 5 с");
            }
            Task unansweredTask = throwUnansweredTask();
            result.setUnansweredTask(unansweredTask);
        }
        LightTask nextTask;
        Category category = getCategory();
        boolean isTournament = category != null && Category.TOUR.equals(category.getId());
        result.setTournament(isTournament);
        if (isTournament) {
            Tournament currentTournament = getTournament();
            if (currentTournament == null) {
                throw new TournamentIsNotSelectedException("Турнир не выбран");
            } else {
                result.setTournament(currentTournament);
                nextTask = taskService.getNextTaskInTournament(currentTournament, currentTask);
                if (nextTask == null) {
                    result.setTournamentStat(answerLogService.getStatForChatUserForTournament(getChatId(), botInfo.getBotUser().getId(),
                            NextTaskResult.STAT_USERS, currentTournament.getId()));
                }
            }
        } else {
            nextTask = taskService.getUnusedByChatTask(getChatId(), category);
        }
        if (nextTask != null) {
            Task task = setCurrentTask(nextTask.getId());
            result.setNextTask(task);
        }
        return result;
    }

    protected Task throwUnansweredTask() {
        Task currentTask = getCurrentTask();
        if (currentTask != null && isTaskUnanswered()) {
            logAnswerOfUser(botInfo.getBotUser(), currentTask.getId());
            return currentTask;
        }
        return null;
    }

    protected boolean isTaskUnanswered() {
        Task currentTask = getCurrentTask();
        return currentTask != null && !answerLogService.isExistByTaskAndChat(currentTask.getId(), getChatId());
    }

    protected Date getUsageTime() {
        Task currentTask = getCurrentTask();
        if (currentTask == null) {
            return null;
        }
        return taskService.getUsageTime(currentTask.getId(), getChatId());
    }

    public synchronized UserAnswerResult userAnswer(UserAnswer userAnswer) {
        String text = userAnswer.getText();
        Long chatId = getChatId();
        User user = userAnswer.getUser();
        UserAnswerResult result = new UserAnswerResult();

        Task currentTask = getCurrentTask();
        if (currentTask != null) {
            Long currentTaskId = currentTask.getId();
            List<Answer> answers = currentTask.getAnswers();
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
                } else if (Answer.Type.APPROXIMATE.equals(answer.getType()) && isApproximatelyCorrect(normalizedText, normalizedAnswer)) {
                    exactAnswer = answer.getText();
                    isCorrect = true;
                } else if (Answer.Type.CONTAINS.equals(answer.getType()) && normalizedText.contains(normalizedAnswer)) {
                    exactAnswer = null;
                    isCorrect = true;
                    break;
                }
            }
            result.setCorrect(isCorrect);
            result.setExactAnswer(exactAnswer);
            result.setCurrentTask(currentTask);
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
                result.setFirstAnsweredUser(firstAnsweredUserId);
                result.setUsageTime(taskService.getUsageTime(currentTaskId, chatId));
            }
        }
        return result;
    }

    public synchronized HintResult getHintForTask(Chat chat, Long taskId) {
        Task currentTask = getCurrentTask();
        HintResult hintResult = new HintResult();

        if (taskId != null && currentTask != null && !taskId.equals(currentTask.getId())) {
            Task requestedTask = taskService.createCompositeTask(taskId);
            if (requestedTask != null) {
                hintResult.setTask(requestedTask);
                hintResult.setTaskCurrent(false);
            }
        } else if (currentTask != null) {
            hintResult.setTask(currentTask);
            hintResult.setTaskCurrent(true);
        }
        if (hintResult.getTask() != null) {
            Long hintTaskId = hintResult.getTask().getId();
            logBotSendHintToChat(chat, hintTaskId);
        }
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

package me.nizheg.telegram.bot.chgk.domain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.TaskScheduler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.PostConstruct;

import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.ScheduledOperation;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;
import me.nizheg.telegram.bot.chgk.exception.GameException;
import me.nizheg.telegram.bot.chgk.service.AnswerLogService;
import me.nizheg.telegram.bot.chgk.service.CategoryService;
import me.nizheg.telegram.bot.chgk.service.ScheduledOperationService;
import me.nizheg.telegram.bot.chgk.service.TaskService;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;
import me.nizheg.telegram.bot.chgk.service.TourService;
import me.nizheg.telegram.bot.chgk.util.BotInfo;
import me.nizheg.telegram.bot.service.PropertyService;

/**
 * @author Nikolay Zhegalin
 */
public class AutoChatGame extends ChatGame {

    private final static String OPERATION_ID_ANSWER = "NextTask";
    private final static String OPERATION_ID_WARNING = "Warning";
    private final static int TIME_WARNING_BEFORE_ANSWER = 10;
    private final static int STATE_STARTED = 0;
    private final static int STATE_PAUSED = 1;
    private final static int STATE_STOPPED = 2;
    private final static int SECOND = 1000;
    private final Map<String, Runner> operationRunners = new HashMap<>();
    private final TaskScheduler taskScheduler;
    private final AnswerOperation answerOperation;
    private final WarningOperation warningOperation;
    private final ScheduledOperationService scheduledOperationService;
    private final int timeout;

    private ScheduledFuture<?> scheduledOperation;
    private ScheduledOperation scheduledOperationLog;
    private volatile int state;

    public AutoChatGame(
            Chat chat,
            int timeout,
            PropertyService propertyService,
            CategoryService categoryService,
            TourService tourService,
            TaskService taskService,
            AnswerLogService answerLogService,
            BotInfo botInfo,
            TelegramUserService telegramUserService,
            TaskScheduler taskScheduler,
            AnswerOperation answerOperation,
            WarningOperation warningOperation,
            ScheduledOperationService scheduledOperationService) {
        super(chat, propertyService, categoryService, tourService, taskService, answerLogService, telegramUserService,
                botInfo);
        this.timeout = timeout;
        this.taskScheduler = taskScheduler;
        this.answerOperation = answerOperation;
        this.warningOperation = warningOperation;
        this.scheduledOperationService = scheduledOperationService;
        operationRunners.put(OPERATION_ID_ANSWER, new AnswerRunner());
        operationRunners.put(OPERATION_ID_WARNING, new WarningRunner());
    }

    @PostConstruct
    @Override
    public synchronized void init() {
        super.init();
        start();
        scheduledOperationLog = scheduledOperationService.getByChatId(chat.getId());
        if (scheduledOperationLog != null) {
            scheduleOperation(scheduledOperationLog.getOperationId(), scheduledOperationLog.getTime());
        }
    }

    public int getTimeout() {
        return timeout;
    }

    private int getTimeoutMillis() {
        return timeout * SECOND;
    }

    public void start() {
        state = STATE_STARTED;
    }

    public synchronized void stop() {
        state = STATE_STOPPED;
        cleanActiveOperation();
    }

    public void pause() {
        state = STATE_PAUSED;
    }

    public synchronized int getTimeLeft() {
        if (scheduledOperation == null || scheduledOperationLog == null) {
            return 0;
        }
        int result = 0;
        int delay = (int) ((scheduledOperationLog.getTime().getTime() - System.currentTimeMillis()) / SECOND);
        String operationId = scheduledOperationLog.getOperationId();
        if (OPERATION_ID_ANSWER.equals(operationId)) {
            result = delay;
        } else if (OPERATION_ID_WARNING.equals(operationId)) {
            result = delay + TIME_WARNING_BEFORE_ANSWER;
        }
        return Math.max(result, 0);
    }

    @Override
    public synchronized NextTaskResult nextTask() throws GameException {
        start();
        NextTaskResult nextTaskResult = super.nextTask();
        scheduleOperation(OPERATION_ID_WARNING, getTimeoutMillis() - TIME_WARNING_BEFORE_ANSWER * SECOND);
        return nextTaskResult;
    }

    @Override
    public synchronized Task repeatTask() {
        start();
        Task task = super.repeatTask();
        if (scheduledOperation == null && isTaskUnanswered()) {
            scheduleOperation(OPERATION_ID_WARNING, getTimeoutMillis() - TIME_WARNING_BEFORE_ANSWER * SECOND);
        }
        return task;
    }

    @Override
    public synchronized UserAnswerResult userAnswer(UserAnswer userAnswer) {
        UserAnswerResult userAnswerResult = super.userAnswer(userAnswer);
        if (userAnswerResult.isCorrect()) {
            cleanActiveOperation();
        }
        return userAnswerResult;
    }

    private synchronized void scheduleOperation(String id, int ms) {
        scheduleOperation(id, new Date(System.currentTimeMillis() + ms));
    }

    private synchronized void scheduleOperation(String id, Date time) {
        Runnable runner = operationRunners.get(id);
        if (runner == null) {
            throw new IllegalStateException("Illegal id of operation. There are no runner for it");
        }
        cancelActiveOperation();
        int currentState = state;
        if (currentState == STATE_STARTED || currentState == STATE_STOPPED) {
            deleteScheduledOperationLog();
        }
        if (currentState == STATE_PAUSED || currentState == STATE_STOPPED) {
            return;
        }
        ScheduledOperation scheduledOperationLog = new ScheduledOperation();
        scheduledOperationLog.setOperationId(id);
        scheduledOperationLog.setTime(time);
        scheduledOperationLog.setChatId(getChatId());
        saveScheduledOperationLog(scheduledOperationLog);
        this.scheduledOperation = taskScheduler.schedule(runner, time);
    }

    private void saveScheduledOperationLog(ScheduledOperation operation) {
        try {
            this.scheduledOperationLog = scheduledOperationService.create(operation);
        } catch (DuplicationException ex) {
            scheduledOperationService.deleteByChatId(operation.getChatId());
            saveScheduledOperationLog(operation);
        }
    }

    private synchronized void cleanActiveOperation() {
        cancelActiveOperation();
        deleteScheduledOperationLog();
    }

    private synchronized void cancelActiveOperation() {
        if (scheduledOperation != null) {
            scheduledOperation.cancel(true);
            scheduledOperation = null;
        }
    }

    private synchronized void deleteScheduledOperationLog() {
        if (scheduledOperationLog != null) {
            scheduledOperationService.deleteByChatId(scheduledOperationLog.getChatId());
            scheduledOperationLog = null;
        }
    }

    private class AnswerRunner extends Runner {

        @Override
        public void doOperation() {
            HintResult hintForTask = getHintForTask(getChat(), null);
            Optional.ofNullable(hintForTask.getTask())
                    .ifPresent(task -> answerOperation.sendAnswerWithRatingAndNextButtons(task, getChatId()));
        }
    }

    private class WarningRunner extends Runner {

        @Override
        public void doOperation() {
            warningOperation.sendTimeWarning(chat, TIME_WARNING_BEFORE_ANSWER);
            scheduleOperation(OPERATION_ID_ANSWER, TIME_WARNING_BEFORE_ANSWER * SECOND);
        }
    }

    private abstract class Runner implements Runnable {

        private final Log logger = LogFactory.getLog(getClass());

        @Override
        public final void run() {
            int currentState = state;
            if (currentState == STATE_STOPPED || currentState == STATE_PAUSED) {
                return;
            }
            try {
                deleteScheduledOperationLog();
                doOperation();
            } catch (RuntimeException ex) {
                logger.error("Unable to do scheduled operation", ex);
            }
        }

        protected abstract void doOperation();
    }

}

package me.nizheg.telegram.bot.chgk.domain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.PostConstruct;

import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.ScheduledOperation;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;
import me.nizheg.telegram.bot.chgk.exception.GameException;
import me.nizheg.telegram.bot.chgk.service.ScheduledOperationService;
import me.nizheg.telegram.util.Emoji;

/**
 * @author Nikolay Zhegalin
 */
@Component("autoChatGame")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AutoChatGame extends ChatGame {

    private final static String OPERATION_ID_NEXT_TASK = "NextTask";
    private final static String OPERATION_ID_WARNING = "Warning";
    private final static int TIME_WARNING_BEFORE_NEXT_TASK = 10;
    private final static int TIME_TO_NEXT_TASK_AFTER_ANSWER = 5;
    private final static int STATE_STARTED = 0;
    private final static int STATE_PAUSED = 1;
    private final static int STATE_STOPPED = 2;
    private final static int SECOND = 1000;
    private final Map<String, Runner> operationRunners = new HashMap<>();
    private final int timeout;
    @Autowired
    private TaskScheduler taskScheduler;
    @Autowired
    private NextTaskOperation nextTaskOperation;
    @Autowired
    private WarningOperation warningOperation;
    @Autowired
    private ScheduledOperationService scheduledOperationService;

    private ScheduledFuture<?> scheduledOperation;
    private ScheduledOperation scheduledOperationLog;
    private long lastAccessedTimeMillis;
    private volatile int state;

    public AutoChatGame(Chat chat, int timeout) {
        super(chat);
        this.timeout = timeout;
        operationRunners.put(OPERATION_ID_NEXT_TASK, new NextTaskRunner());
        operationRunners.put(OPERATION_ID_WARNING, new WarningRunner());
    }

    @PostConstruct
    @Override
    public synchronized void init() {
        super.init();
        resetAccessTime();
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
        resetAccessTime();
        cancelPreviousOperation();
        deleteScheduledOperationLog();
    }

    public void pause() {
        state = STATE_PAUSED;
    }

    private void resetAccessTime() {
        lastAccessedTimeMillis = 0;
    }

    private boolean isAccessTimeUnknown() {
        return lastAccessedTimeMillis == 0;
    }

    private void refreshAccessTime() {
        lastAccessedTimeMillis = System.currentTimeMillis();
    }

    public synchronized int getTimeToNextTask() {
        if (scheduledOperation == null || scheduledOperationLog == null) {
            return 0;
        }
        int result = 0;
        int delay = (int) ((scheduledOperationLog.getTime().getTime() - System.currentTimeMillis()) / SECOND);
        String operationId = scheduledOperationLog.getOperationId();
        if (OPERATION_ID_NEXT_TASK.equals(operationId)) {
            result = delay;
            if (isTaskUnanswered()) {
                result += TIME_TO_NEXT_TASK_AFTER_ANSWER;
            }
        } else if (OPERATION_ID_WARNING.equals(operationId)) {
            result = delay + TIME_WARNING_BEFORE_NEXT_TASK;
        }
        return Math.max(result, 0);
    }

    @Override
    public synchronized NextTaskResult nextTask() throws GameException {
        wakeUp();
        if (System.currentTimeMillis() - lastAccessedTimeMillis < 3 * getTimeoutMillis()) {
            NextTaskResult nextTaskResult = super.nextTask();
            scheduleOperation(OPERATION_ID_WARNING, getTimeoutMillis() - TIME_WARNING_BEFORE_NEXT_TASK * SECOND);
            return nextTaskResult;
        } else {
            stop();
            throw new GameException(Emoji.SLEEPING_SYMBOL);
        }
    }

    @Override
    public synchronized Task repeatTask() {
        wakeUp();
        Task task = super.repeatTask();
        if (scheduledOperation == null) {
            scheduleOperation(OPERATION_ID_WARNING, getTimeoutMillis() - TIME_WARNING_BEFORE_NEXT_TASK * SECOND);
        }
        return task;
    }

    private void wakeUp() {
        start();
        if (isAccessTimeUnknown()) {
            refreshAccessTime();
        }
    }

    @Override
    public synchronized HintResult getHintForTask(Chat chat, Long taskId) {
        refreshAccessTime();
        return super.getHintForTask(chat, taskId);
    }

    @Override
    public synchronized UserAnswerResult userAnswer(UserAnswer userAnswer) {
        refreshAccessTime();
        UserAnswerResult userAnswerResult = super.userAnswer(userAnswer);
        if (userAnswerResult.isCorrect()) {
            scheduleOperation(OPERATION_ID_NEXT_TASK, TIME_TO_NEXT_TASK_AFTER_ANSWER * SECOND);
        }
        return userAnswerResult;
    }

    protected synchronized void scheduleOperation(String id, int ms) {
        scheduleOperation(id, new Date(System.currentTimeMillis() + ms));
    }

    protected synchronized void scheduleOperation(String id, Date time) {
        Runnable runner = operationRunners.get(id);
        if (runner == null) {
            throw new IllegalStateException("Illegal id of operation. There are no runner for it");
        }
        cancelPreviousOperation();
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

    protected synchronized void cancelPreviousOperation() {
        if (scheduledOperation != null) {
            scheduledOperation.cancel(true);
            scheduledOperation = null;
        }
    }

    protected synchronized void deleteScheduledOperationLog() {
        if (scheduledOperationLog != null) {
            scheduledOperationService.deleteByChatId(scheduledOperationLog.getChatId());
            scheduledOperationLog = null;
        }
    }

    private class NextTaskRunner extends Runner {
        @Override
        public void doOperation() {
            Task unansweredTask = throwUnansweredTask();
            if (unansweredTask != null) {
                nextTaskOperation.sendAnswerOfPreviousTask(getChat(), unansweredTask);
                scheduleOperation(OPERATION_ID_NEXT_TASK, TIME_TO_NEXT_TASK_AFTER_ANSWER * SECOND);
            } else {
                nextTaskOperation.sendNextTask(AutoChatGame.this);
            }
        }
    }

    private class WarningRunner extends Runner {
        @Override
        public void doOperation() {
            warningOperation.sendTimeWarning(chat, TIME_WARNING_BEFORE_NEXT_TASK);
            scheduleOperation(OPERATION_ID_NEXT_TASK, TIME_WARNING_BEFORE_NEXT_TASK * SECOND);
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
                doOperation();
            } catch (RuntimeException ex) {
                logger.error("Unable to do scheduled operation", ex);
                cancelPreviousOperation();
                deleteScheduledOperationLog();
            }
        }

        protected abstract void doOperation();
    }

}

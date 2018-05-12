package me.nizheg.telegram.bot.chgk.service.impl;

import me.nizheg.telegram.bot.chgk.service.MessageReceiverService;
import me.nizheg.telegram.bot.service.PropertyService;
import me.nizheg.telegram.bot.service.impl.MessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ScheduledFuture;

@Service
public class MessageReceiverServiceImpl implements MessageReceiverService {
    private final Log logger = LogFactory.getLog(getClass());
    private final TaskScheduler taskScheduler;
    private final MessageReceiver messageReceiver;
    private final PropertyService propertyService;
    private volatile ScheduledFuture<?> scheduledReceiving;

    public MessageReceiverServiceImpl(TaskScheduler taskScheduler, MessageReceiver messageReceiver, PropertyService propertyService) {
        this.taskScheduler = taskScheduler;
        this.messageReceiver = messageReceiver;
        this.propertyService = propertyService;
    }

    @PostConstruct
    public void init() {
        Long delay = propertyService.getLongValue("bot.message.receiving.delay.ms");
        if (logger.isDebugEnabled()) {
            logger.debug("Read value of message receiving: " + delay);
        }
        if (delay != null) {
            startReceiving(delay);
        }
    }

    @Override
    public void startReceiving(long delayInMs) {
        synchronized (this) {
            if (logger.isDebugEnabled()) {
                logger.debug("Schedule message receiving with delay in ms: " + delayInMs);
            }
            scheduledReceiving = taskScheduler.scheduleWithFixedDelay(messageReceiver::receive, delayInMs);
        }
    }

    @Override
    public void stopReceiving() {
        synchronized (this) {
            if (scheduledReceiving != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Stopping message receiving");
                }
                scheduledReceiving.cancel(true);
                scheduledReceiving = null;
                if (logger.isDebugEnabled()) {
                    logger.debug("Message receiving is stopped");
                }
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        stopReceiving();
    }
}

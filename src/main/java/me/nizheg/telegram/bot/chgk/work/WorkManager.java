package me.nizheg.telegram.bot.chgk.work;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import me.nizheg.util.NamedThreadFactory;

@CommonsLog
@RequiredArgsConstructor
public class WorkManager {

    private final WorkService workService;
    private volatile int batchSize;
    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture<?> scheduledFuture;

    @NonNull
    private final List<Worker> workers = new CopyOnWriteArrayList<>();

    public void registerWorker(@NonNull Worker worker) {
        workers.add(worker);
    }

    public synchronized void start(long period, TimeUnit unit, int batchSize) {
        this.batchSize = batchSize;
        if (this.scheduledExecutorService == null) {
            this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
                    new NamedThreadFactory("worker-"));
        }
        if (this.scheduledFuture == null) {
            this.scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(this::processWork, period, period,
                    unit);
        }
    }

    public synchronized void stop() {
        if (this.scheduledFuture != null) {
            this.scheduledFuture.cancel(false);
            this.scheduledFuture = null;
        }
    }

    public synchronized void shutdown() {
        this.scheduledExecutorService.shutdown();
        this.scheduledExecutorService = null;
        this.scheduledFuture = null;
    }

    public synchronized boolean isStarted() {
        return this.scheduledFuture != null && this.scheduledExecutorService != null &&
                !this.scheduledExecutorService.isShutdown();
    }

    private void processWork() {
        log.debug("Start works processing");
        List<WorkDescription> works;
        try {
            works = getWorks(batchSize);
        } catch (RuntimeException ex) {
            log.error("Failed to retrieve works", ex);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("There is founded " + works.size() + " works");
        }
        for (WorkDescription work : works) {
            try {
                doWork(work);
            } catch (RuntimeException e) {
                if (log.isErrorEnabled()) {
                    log.error("Error during execution of " + work, e);
                }
            }
        }
        log.debug(("Works processing is finished"));
    }

    private List<WorkDescription> getWorks(int count) {
        return workService.getWorks(count, WorkStatus.READY);
    }


    private void doWork(WorkDescription workDescription) {
        if (log.isDebugEnabled()) {
            log.debug("Process work " + workDescription);
        }
        for (Worker worker : workers) {
            if (worker.canDo(workDescription)) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Worker found: " + worker);
                    }
                    workService.changeStatus(workDescription, WorkStatus.STARTED);
                    worker.doWork(workDescription);
                    workService.changeStatus(workDescription, WorkStatus.FINISHED);
                    break;
                } catch (WorkException | RuntimeException ex) {
                    if (log.isErrorEnabled()) {
                        log.error("Error during execution of " + workDescription);
                    }
                    workService.changeStatus(workDescription, WorkStatus.ERROR);
                }
            }
        }
    }


}

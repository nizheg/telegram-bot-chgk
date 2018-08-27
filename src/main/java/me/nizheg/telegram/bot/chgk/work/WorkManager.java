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

    private void processWork() {
        log.debug("Start works processing");
        List<WorkDescription> works = getWork(batchSize);
        if (log.isDebugEnabled()) {
            log.debug("There is founded " + works.size() + " works");
        }
        for (WorkDescription work : works) {
            doWork(work);
        }
        log.debug(("Works processing is finished"));
    }

    private List<WorkDescription> getWork(int count) {
        return workService.getWorks(count, WorkStatus.CREATED);
    }


    private void doWork(WorkDescription workDescription) {
        if (log.isDebugEnabled()) {
            log.debug("Process work " + workDescription);
        }
        for (Worker worker : workers) {
            if (worker.canDo(workDescription)) {
                if (log.isDebugEnabled()) {
                    log.debug("Worker found: " + worker);
                }
                workService.changeStatus(workDescription, WorkStatus.STARTED);
                worker.doWork(workDescription);
                workService.changeStatus(workDescription, WorkStatus.FINISHED);
                break;
            }
        }
    }


}

package me.nizheg.telegram.bot.chgk.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.nizheg.telegram.bot.chgk.service.TaskService;
import me.nizheg.telegram.bot.chgk.work.WorkManager;

@RestController
@RequestMapping("api/manage")
@RequiredArgsConstructor
public class ManagementController {

    private final WorkManager workManager;
    private final TaskService taskService;

    @PostMapping("works")
    public void start(@RequestBody WorksConfig worksConfig) {
        workManager.start(worksConfig.periodInSeconds, TimeUnit.SECONDS, worksConfig.batchSize);
    }

    @DeleteMapping("works")
    public void stop() {
        workManager.stop();
    }

    @GetMapping("works/status")
    public WorksConfig getStatus() {
        boolean isStarted = workManager.isStarted();
        int period = workManager.getPeriod();
        int batchSize = workManager.getBatchSize();
        return new WorksConfig(period, batchSize, isStarted);

    }

    @RequestMapping(value = "/tasks/archive", method = RequestMethod.POST)
    public int archiveTasks() {
        return taskService.archiveTasks();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class WorksConfig {
        int periodInSeconds =1;
        int batchSize = 1;
        boolean isStarted;
    }
}

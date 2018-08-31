package me.nizheg.telegram.bot.chgk.web;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.chgk.service.TaskService;
import me.nizheg.telegram.bot.chgk.work.WorkManager;

@RestController
@RequestMapping("api/manage")
@RequiredArgsConstructor
public class ManagementController {

    private final WorkManager workManager;
    private final TaskService taskService;

    @GetMapping("works")
    public void start(@RequestParam("period") long periodInSeconds, @RequestParam int batchSize) {
        workManager.start(periodInSeconds, TimeUnit.SECONDS, batchSize);
    }

    @DeleteMapping("works")
    public void stop() {
        workManager.stop();
    }

    @RequestMapping(value = "/tasks/archive", method = RequestMethod.POST)
    public String archiveTasks() {
        int archived = taskService.archiveTasks();
        return "Archived: " + archived;
    }
}

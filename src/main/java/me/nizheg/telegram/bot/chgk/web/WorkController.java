package me.nizheg.telegram.bot.chgk.web;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.chgk.work.WorkManager;

@RestController
@RequestMapping("api/works")
@RequiredArgsConstructor
public class WorkController {

    private final WorkManager workManager;

    @GetMapping("service")
    public void start(@RequestParam("period") long periodInSeconds, @RequestParam int batchSize) {
        workManager.start(periodInSeconds, TimeUnit.SECONDS, batchSize);
    }

    @DeleteMapping("service")
    public void stop() {
        workManager.stop();
    }
}

package org.example.buttonforload.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.buttonforload.service.FilePollingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileImportScheduler implements TaskExecutorScheduler {

    private final FilePollingService filePollingService;

    @Override
    @Scheduled(cron = "${app.scheduler.scan-cron}")
    public void executeTask() {
        filePollingService.scanDirectory();
    }
}
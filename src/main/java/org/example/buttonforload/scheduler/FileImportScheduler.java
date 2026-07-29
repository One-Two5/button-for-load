package org.example.buttonforload.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.buttonforload.service.ScanDirectoryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileImportScheduler {

    private final ScanDirectoryService scanDirectoryService;

    @Scheduled(cron = "${app.scheduler.scan-cron}")
    public synchronized void scan() {
        scanDirectoryService.scanDirectory();
    }
}

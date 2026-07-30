package org.example.buttonforload.scheduler;

import org.example.buttonforload.service.FilePollingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskExecutorSchedulerImpl implements TaskExecutorScheduler {

    private static final Logger log = LoggerFactory.getLogger(TaskExecutorSchedulerImpl.class);
    private final FilePollingService filePollingService;
    private final AsyncTaskExecutor taskExecutor;

    public TaskExecutorSchedulerImpl(FilePollingService filePollingService, @Qualifier("applicationTaskExecutor") AsyncTaskExecutor taskExecutor) {
        this.filePollingService = filePollingService;
        this.taskExecutor = taskExecutor;
    }

    @Override
    @Scheduled(cron = "${app.scheduler.scan-cron}")
    public void executeTask() {
        List<Runnable> tasksList = List.of(
                filePollingService::scanDirectory
        );

        log.info("Планировщик обнаружил {} задачу. Запуск параллельного цикла...", tasksList.size());

        for (Runnable task : tasksList) {
            taskExecutor.execute(task);
        }
        log.info("Планировщик успешно распределил все задачи из списка по потокам.");
    }
}
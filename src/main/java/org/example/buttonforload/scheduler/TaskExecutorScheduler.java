package org.example.buttonforload.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskExecutorScheduler {

    private static final Logger log = LoggerFactory.getLogger(TaskExecutorScheduler.class);
    private final List<TaskExecutor> schedulers;
    private final AsyncTaskExecutor applicationTaskExecutor;

    public TaskExecutorScheduler(List<TaskExecutor> schedulers,
                                     @Qualifier("applicationTaskExecutor") AsyncTaskExecutor applicationTaskExecutor) {
        this.schedulers = schedulers;
        this.applicationTaskExecutor = applicationTaskExecutor;
    }

    @Scheduled(cron = "${app.scheduler.scan-cron}")
    public void run() {
        if (schedulers.isEmpty()) {
            log.info("Список задач пуст.");
            return;
        }

        log.info("Планировщик обнаружил {} задачу. Запуск параллельного цикла...", schedulers.size());

        for (TaskExecutor scheduler : schedulers) {
            applicationTaskExecutor.execute(() -> {
                try {
                    scheduler.executeTask();
                } catch (Exception e) {
                    log.error("Ошибка при выполнении задачи в потоке: {}", scheduler.getClass().getName(), e);
                }
            });
        }
        log.info("Планировщик успешно распределил все задачи из списка по потокам.");
    }
}
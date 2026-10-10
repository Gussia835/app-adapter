package foodcards.adapter.service;

import foodcards.adapter.mapper.AppAdapterConfigMapper;
import foodcards.adapter.models.AppAdapterConfigEntity;
import foodcards.adapter.service.producer.WorkerTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskRunnerService implements CommandLineRunner {
    private final ObjectProvider<WorkerTask> workerProvider;
    private final AppAdapterConfigMapper configMapper;

    @Override
    public void run(String... args) {
        log.info("Начинается работа потоков");
        List<AppAdapterConfigEntity> configs = configMapper.findAll();

        for (AppAdapterConfigEntity config : configs) {
            if (config.getStatusOut() != null && config.getStatusOut() > 0) {
                WorkerTask task = workerProvider.getObject();
                task.start(config);
            }
        }
    }
}
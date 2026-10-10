package foodcards.adapter.service.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerManagerService {

    private final ObjectProvider<WorkerTask> workerProvider;
    private final Map<String, WorkerTask> activeWorkers = new ConcurrentHashMap<>();
    private final AtomicInteger workerCounter = new AtomicInteger(0);

    public void registerWorker(String entityType, WorkerTask worker) {
        activeWorkers.put(entityType, worker);
        workerCounter.incrementAndGet();
        log.info("Зарегистрирован воркер для {}. Всего активных: {}", entityType, workerCounter.get());
    }

    public void unregisterWorker(String entityType) {
        activeWorkers.remove(entityType);
        workerCounter.decrementAndGet();
        log.info("Воркер для {} завершен. Осталось активных: {}", entityType, workerCounter.get());
    }

    public int getActiveWorkersCount() {
        return workerCounter.get();
    }

    public Map<String, WorkerTask> getActiveWorkers() {
        return new HashMap<>(activeWorkers);
    }

    public void restartAllWorkers() {
        log.info("Перезапуск всех воркеров...");
        activeWorkers.forEach((entityType, worker) -> {
            worker.shutDown();
            log.info("Воркер {} остановлен для перезапуска", entityType);
        });
        activeWorkers.clear();
        workerCounter.set(0);
    }

    public void restartWorker(String entityType) {
        WorkerTask worker = activeWorkers.get(entityType);
        if (worker != null) {
            worker.shutDown();
            activeWorkers.remove(entityType);
            workerCounter.decrementAndGet();
            log.info("Воркер {} остановлен для перезапуска", entityType);
        }
    }
}

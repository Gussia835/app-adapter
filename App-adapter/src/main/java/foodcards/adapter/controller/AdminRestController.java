package foodcards.adapter.controller;

import foodcards.adapter.dao.AppAdapterDAO;
import foodcards.adapter.service.producer.WorkerManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminRestController {

    private final AppAdapterDAO dao;
    private final WorkerManagerService workerManager;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> response = new HashMap<>();
        response.put("adapter_transactions", dao.getTransactionStats());
        response.put("active_workers", workerManager.getActiveWorkersCount());
        response.put("service_status", "RUNNING");
        return response;
    }

    @GetMapping("/workers")
    public Map<String, Object> getWorkersInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("active_count", workerManager.getActiveWorkersCount());
        response.put("workers", workerManager.getActiveWorkers().keySet());
        return response;
    }

    @PostMapping("/workers/restart")
    public ResponseEntity<Map<String, String>> restartAllWorkers() {
        workerManager.restartAllWorkers();
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Все воркеры перезапущены");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/workers/restart/{entityType}")
    public ResponseEntity<Map<String, String>> restartWorker(@PathVariable String entityType) {
        workerManager.restartWorker(entityType);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Воркер " + entityType + " перезапущен");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public Map<String, Object> detailedHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("active_workers", workerManager.getActiveWorkersCount());
        health.put("transactions", dao.getTransactionStats());
        return health;
    }
}

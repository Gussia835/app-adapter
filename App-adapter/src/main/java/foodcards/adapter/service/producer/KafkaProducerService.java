package foodcards.adapter.service.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import foodcards.adapter.dto.KafkaSendDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public String toJson(KafkaSendDTO payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Ошибка сериализации JSON", e);
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    public void send(String topic, String requestId, String jsonMessage) {
        kafkaTemplate.send(topic, requestId, jsonMessage);
        log.info("Отправлено в kafka: {} в топик {}", requestId, topic);
    }
}
package foodcards.adapter.service.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageListener {
    private final KafkaMessageProcessor messageProcessor;

    @KafkaListener(
            topics = "#{@kafkaTopicProvider.getInboundTopics()}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListener"
    )
    public void onMessage(String jsonMessage, Acknowledgment ack) {
        log.info("Получено сообщение из Kafka");
        try {
            messageProcessor.process(jsonMessage);
            ack.acknowledge();
            log.info("Сообщение успешно обработано и закоммичено");
        } catch (Exception e) {
            log.error("Ошибка. сообщение вернется в Kafka", e);
        }
    }
}
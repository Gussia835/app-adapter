package foodcards.adapter.service.producer;

import foodcards.adapter.builder.KafkaBuilder;
import foodcards.adapter.dao.AppAdapterDAO;
import foodcards.adapter.dto.KafkaSendDTO;
import foodcards.adapter.models.GruVistaTabEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class TaskGeneratorService {
    private final AppAdapterDAO adapterDAO;
    private final KafkaBuilder kafkaBuilder;

    @Value("${app.kafka.batch-size:100}")
    private int BATCH_SIZE;

    public KafkaSendDTO generateDTO(String entityType, String eventType) {
        List<GruVistaTabEntity> batch = adapterDAO.getWaiting(BATCH_SIZE);

        if (batch != null && !batch.isEmpty()) {
            log.info("Сгенерирован батч. Записей: {}", batch.size());

            return kafkaBuilder.buildKafkaSend(batch, eventType, entityType);
        }

        return null;
    }

}

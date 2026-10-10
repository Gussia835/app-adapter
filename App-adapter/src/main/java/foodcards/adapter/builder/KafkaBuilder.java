package foodcards.adapter.builder;

import foodcards.adapter.dto.EventSendDTO;
import foodcards.adapter.dto.KafkaSendDTO;
import foodcards.adapter.models.GruVistaTabEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class KafkaBuilder {

    public KafkaSendDTO buildKafkaSend(List<GruVistaTabEntity> entityList, String eventType, String entityType) {
        String requestId = "req-" + UUID.randomUUID().toString();

        return KafkaSendDTO.builder()
                .actualTimestamp(String.valueOf(Instant.now().toEpochMilli()))
                .systemId("GRU")
                .requestId(requestId)
                .eventType(eventType)
                .entityType(entityType)
                .events(buildEventsList(entityList))
                .build();
    }

    private List<EventSendDTO> buildEventsList(List<GruVistaTabEntity> records) {
        return records.stream()
                .map(record -> EventSendDTO.builder()
                        .id(record.getId().toString())
                        .systemAccount(record.getSystemAccount())
                        .currency(record.getCurrency())
                        .xalfa(record.getXalfa() != null ? record.getXalfa().toString() : "0")
                        .operation(record.getOperation())
                        .build())
                .collect(Collectors.toList());
    }
}

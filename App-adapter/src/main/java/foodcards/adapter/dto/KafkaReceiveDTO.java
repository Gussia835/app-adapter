package foodcards.adapter.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KafkaReceiveDTO {
    private String actualTimestamp;
    private String systemId;
    private String requestId;
    private String eventType;
    private String entityType;
    private String status;
    private List<EventReceiveDTO> events;
    private DataErrorDTO error;
}

package foodcards.adapter.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KafkaSendDTO {
    private String actualTimestamp;
    private String systemId;
    private String requestId;
    private String eventType;
    private String entityType;
    private List<EventSendDTO> events;
}

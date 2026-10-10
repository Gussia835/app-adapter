package foodcards.adapter.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventReceiveDTO {
    private String entityValue;
    private String entityId;
    private DataSuccessDTO data;
    private String status;
    private DataErrorDTO error;
}

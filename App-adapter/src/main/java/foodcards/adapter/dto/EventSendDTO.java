package foodcards.adapter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventSendDTO {
    private String id;
    private String systemAccount;
    private String currency;

    @JsonProperty("XALFA")
    private String xalfa;

    private String operation;
}

package foodcards.adapter.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataErrorDTO {
    private String code;
    private String message;
}

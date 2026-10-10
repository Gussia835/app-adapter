package foodcards.adapter.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataSuccessDTO {
    private String oldTbal;
    private String newTbal;
    private String operation;
}

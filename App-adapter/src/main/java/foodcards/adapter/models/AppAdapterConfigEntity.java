package foodcards.adapter.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppAdapterConfigEntity {
    private Long id;
    private String systemId;
    private String eventType;
    private String entityType;
    private String topicIn;
    private Integer statusIn;
    private String topicOut;
    private Integer statusOut;
    private LocalDateTime insTs;
    private LocalDateTime updTs;
}

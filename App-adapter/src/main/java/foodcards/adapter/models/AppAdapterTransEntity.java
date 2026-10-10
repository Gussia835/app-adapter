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
public class AppAdapterTransEntity {
    private Long id;
    private String systemId;
    private String requestId;
    private String eventType;
    private String data;
    private String status;
    private String respCode;
    private String respDesc;
    private LocalDateTime insTs;
    private LocalDateTime updTs;
}

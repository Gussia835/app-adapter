package foodcards.adapter.builder;

import foodcards.adapter.dto.DataErrorDTO;
import foodcards.adapter.models.AppAdapterIoMsgsEntity;
import foodcards.adapter.models.AppAdapterTransEntity;
import foodcards.adapter.models.GruRejectTabEntity;
import org.springframework.stereotype.Component;

import static foodcards.adapter.utils.Constants.*;

@Component
public class EntityBuilder {

    public AppAdapterTransEntity buildTrans(String requestId, String eventType, String jsonMessage) {
        return AppAdapterTransEntity.builder()
                .systemId(SYSTEM_ID)
                .requestId(requestId)
                .eventType(eventType)
                .data(jsonMessage)
                .status(STATUS_PROGRESS)
                .build();
    }

    public AppAdapterIoMsgsEntity buildIoMsg(Long transId, String msgType, String dir, String jsonMessage) {
        return AppAdapterIoMsgsEntity.builder()
                .transId(transId)
                .msgType(msgType)
                .dir(dir)
                .msg(jsonMessage)
                .build();
    }

    public GruRejectTabEntity buildRejectTab(String entityId, String entityValue, DataErrorDTO error) {
        return GruRejectTabEntity.builder()
                .vistaTabId(Long.parseLong(entityId))
                .systemAccount(entityValue)
                .rejectDesc(error != null ? error.getMessage() : BATCH_ERROR)
                .checkStatus(STATUS_ERROR)
                .build();
    }
}
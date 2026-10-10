package foodcards.adapter.service.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import foodcards.adapter.builder.EntityBuilder;
import foodcards.adapter.dao.AppAdapterDAO;
import foodcards.adapter.dto.KafkaReceiveDTO;
import foodcards.adapter.models.AppAdapterIoMsgsEntity;
import foodcards.adapter.models.AppAdapterTransEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import static foodcards.adapter.utils.Constants.DIR_IN;
import static foodcards.adapter.utils.Constants.STATUS_ERROR;
import static foodcards.adapter.utils.Constants.STATUS_SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageProcessor {
    private final ObjectMapper objectMapper;
    private final AppAdapterDAO dao;
    private final EntityBuilder entityBuilder;

    @Transactional
    public void process(String jsonMessage) throws IOException {
        KafkaReceiveDTO receiveDTO = objectMapper.readValue(jsonMessage, KafkaReceiveDTO.class);
        String requestId = receiveDTO.getRequestId();

        AppAdapterTransEntity trans = dao.findTransByRequestId(requestId);
        if (trans == null) {
            log.warn("Неизвестный requestId: {}. Игнорируем", requestId);
            return;
        }

        AppAdapterIoMsgsEntity ioMsg = entityBuilder.buildIoMsg(trans.getId(), trans.getEventType(), DIR_IN, jsonMessage);
        dao.saveInboundAudit(ioMsg);

        if (STATUS_ERROR.equals(receiveDTO.getStatus()) || receiveDTO.getEvents() == null) {
            handleBatchError(receiveDTO);
        } else {
            handleBatchSuccess(receiveDTO);
        }

        dao.markTransactionAsProcessed(requestId);
        log.info("Статус транзакции обновлен на SUCCESS. requestId: {}", requestId);
    }

    private void handleBatchSuccess(KafkaReceiveDTO dto) {
        for (var event : dto.getEvents()) {
            if (STATUS_SUCCESS.equals(event.getStatus()) && event.getData() != null) {
                dao.updateVistaTabSuccess(
                        Long.parseLong(event.getEntityId()),
                        new BigDecimal(event.getData().getNewTbal()),
                        new BigDecimal(event.getData().getOldTbal())
                );
            } else if (STATUS_ERROR.equals(event.getStatus())) {
                dao.updateVistaTabToError(List.of(Long.parseLong(event.getEntityId())));
                dao.saveReject(entityBuilder.buildRejectTab(event.getEntityId(), event.getEntityValue(), event.getError()));
            }
        }
    }

    private void handleBatchError(KafkaReceiveDTO dto) {
        if (dto.getEvents() != null) {
            List<Long> errorIds = dto.getEvents().stream().map(e -> Long.parseLong(e.getEntityId())).toList();
            dao.updateVistaTabToError(errorIds);
            for (var event : dto.getEvents()) {
                dao.saveReject(entityBuilder.buildRejectTab(event.getEntityId(), event.getEntityValue(), event.getError()));
            }
        }
    }
}
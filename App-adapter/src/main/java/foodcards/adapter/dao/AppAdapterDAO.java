package foodcards.adapter.dao;

import foodcards.adapter.mapper.AppAdapterIoMsgsMapper;
import foodcards.adapter.mapper.AppAdapterTransMapper;
import foodcards.adapter.mapper.GruRejectTabMapper;
import foodcards.adapter.mapper.GruVistaTabMapper;
import foodcards.adapter.models.AppAdapterIoMsgsEntity;
import foodcards.adapter.models.AppAdapterTransEntity;
import foodcards.adapter.models.GruRejectTabEntity;
import foodcards.adapter.models.GruVistaTabEntity;
import foodcards.adapter.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static foodcards.adapter.utils.Constants.*;

@Repository
@RequiredArgsConstructor
public class AppAdapterDAO {
    private final GruVistaTabMapper gruVistaTabMapper;
    private final AppAdapterTransMapper transMapper;
    private final AppAdapterIoMsgsMapper ioMsgsMapper;
    private final GruRejectTabMapper rejectTabMapper;

    @Value("${app.kafka.batch-size:100}")
    public int BATCH_SIZE;

    @Transactional
    public List<GruVistaTabEntity> getWaiting(int batchSize) {
        List<GruVistaTabEntity> records = gruVistaTabMapper.selectWaitingRecords(BATCH_SIZE);

        if (records != null && !records.isEmpty()) {
            List<Long> ids = records.stream().map(GruVistaTabEntity::getId).toList();
            gruVistaTabMapper.updateStatusToProgress(ids);
        }
        return records;
    }

    @Transactional
    public void saveOutboundAudit(AppAdapterTransEntity trans, AppAdapterIoMsgsEntity ioMsg) {
        transMapper.insert(trans);
        ioMsg.setTransId(trans.getId());
        ioMsgsMapper.insert(ioMsg);
    }

    @Transactional
    public void markTransactionAsSentToKafka(String requestId) {
        AppAdapterTransEntity trans = transMapper.findByRequestId(requestId);
        if (trans != null) {
            transMapper.updateStatus(trans.getId(), STATUS_SENT_TO_KAFKA);
        }
    }

    @Transactional
    public void markTransactionAsProcessed(String requestId) {
        AppAdapterTransEntity trans = transMapper.findByRequestId(requestId);
        if (trans != null) {
            transMapper.updateStatus(trans.getId(), STATUS_PROCESSED);
        }
    }

    @Transactional
    public void saveInboundAudit(AppAdapterIoMsgsEntity ioMsg) {
        ioMsgsMapper.insert(ioMsg);
    }

    public void updateVistaTabSuccess(Long id, BigDecimal newTbal, BigDecimal oldTbal) {
        gruVistaTabMapper.updateDataSuccess(id, newTbal, oldTbal);
    }

    public void updateVistaTabToError(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            gruVistaTabMapper.updateStatusToError(ids);
        }
    }

    public void saveReject(GruRejectTabEntity reject) {
        rejectTabMapper.insert(reject);
    }

    public AppAdapterTransEntity findTransByRequestId(String requestId) {
        return transMapper.findByRequestId(requestId);
    }

    public Map<String, Integer> getTransactionStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("WAIT", transMapper.countByStatus(STATUS_WAIT));
        stats.put("PROGRESS", transMapper.countByStatus(STATUS_PROGRESS));
        stats.put("SENT_TO_KAFKA", transMapper.countByStatus(STATUS_SENT_TO_KAFKA));
        stats.put("PROCESSED", transMapper.countByStatus(STATUS_PROCESSED));
        stats.put("ERROR", transMapper.countByStatus(STATUS_ERROR));
        return stats;
    }
}
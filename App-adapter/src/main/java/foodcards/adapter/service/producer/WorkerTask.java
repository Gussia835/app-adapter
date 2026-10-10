package foodcards.adapter.service.producer;

import foodcards.adapter.builder.EntityBuilder;
import foodcards.adapter.dao.AppAdapterDAO;
import foodcards.adapter.dto.KafkaSendDTO;
import foodcards.adapter.models.AppAdapterConfigEntity;
import foodcards.adapter.models.AppAdapterIoMsgsEntity;
import foodcards.adapter.models.AppAdapterTransEntity;
import foodcards.adapter.utils.TimeParserUtil;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicBoolean;

import static foodcards.adapter.utils.Constants.*;

@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class WorkerTask {
    private final TaskGeneratorService taskGeneratorService;
    private final KafkaProducerService producerService;
    private final AppAdapterDAO dao;
    private final TimeParserUtil timeParser;
    private final EntityBuilder entityBuilder;
    private final WorkerManagerService workerManager;

    private String currentEntityType;

    @Value("${app.kafka.sleep-time-ms:" + DEFAULT_SLEEP_TIME + "}")
    private String sleepTimeConfig;

    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    @Async("workerTaskExecutor")
    public void start(AppAdapterConfigEntity config) {
        this.currentEntityType = config.getEntityType();
        workerManager.registerWorker(this.currentEntityType, this);

        long sleepTime = timeParser.parseToMillis(sleepTimeConfig);

        while (isRunning.get()) {
            try {
                KafkaSendDTO sendDTO = taskGeneratorService.generateDTO(config.getEntityType(), config.getEventType());

                if (sendDTO != null) {
                    String json = producerService.toJson(sendDTO);

                    AppAdapterTransEntity trans = entityBuilder.buildTrans(sendDTO.getRequestId(), config.getEventType(), json);
                    trans.setStatus(STATUS_SENT_TO_KAFKA);

                    AppAdapterIoMsgsEntity ioMsg = entityBuilder.buildIoMsg(null, config.getEventType(), DIR_OUT, json);
                    dao.saveOutboundAudit(trans, ioMsg);

                    producerService.send(config.getTopicOut(), sendDTO.getRequestId(), json);
                    dao.markTransactionAsSentToKafka(sendDTO.getRequestId());
                } else {
                    Thread.sleep(sleepTime);
                }
            } catch (InterruptedException e) {
                log.error("Поток {} прерван", config.getEntityType());
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Ошибка в цикле {}", config.getEntityType(), e);
                try {
                    Thread.sleep(timeParser.parseToMillis(DEFAULT_SLEEP_TIME));

                } catch (InterruptedException ie) {
                   break;
                }
            }
        }
    }

    @PreDestroy
    public void shutDown() {
        log.info("Завершаем поток");
        this.isRunning.set(false);

        if (this.currentEntityType != null) {
            workerManager.unregisterWorker(this.currentEntityType);
        }
    }
}
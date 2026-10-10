package foodcards.adapter.config;

import foodcards.adapter.mapper.AppAdapterConfigMapper;
import foodcards.adapter.models.AppAdapterConfigEntity;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Slf4j
public class KafkaTopicProvider {
    private final AppAdapterConfigMapper configMapper;
    private String inboundTopics = "gru.balance.in";

    @PostConstruct
    public void init() {
        List<AppAdapterConfigEntity> configs = configMapper.findAll();
        List<String> topics = configs.stream()
                .filter(c -> c.getStatusIn() > 0)
                .map(AppAdapterConfigEntity::getTopicIn)
                .collect(Collectors.toList());

        if (!topics.isEmpty()) {
            this.inboundTopics = String.join(",", topics);
            log.info("Динамически загружены входящие топики для прослушивания: {}", this.inboundTopics);
        }
    }

    public String getInboundTopics() {
        return inboundTopics;
    }
}

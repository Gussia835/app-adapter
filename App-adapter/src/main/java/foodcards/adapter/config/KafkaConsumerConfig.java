package foodcards.adapter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import foodcards.adapter.dto.KafkaReceiveDTO;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, KafkaReceiveDTO> consumerFactory(KafkaProperties kafkaProperties,
                                                                    ObjectMapper objectMapper) {

        Map<String, Object> configs = kafkaProperties.buildConsumerProperties(null);
        DefaultKafkaConsumerFactory<String, KafkaReceiveDTO> factory = new DefaultKafkaConsumerFactory<>(configs);

        factory.setValueDeserializerSupplier(() -> {
            JsonDeserializer<KafkaReceiveDTO> deserializer = new JsonDeserializer<>(KafkaReceiveDTO.class, objectMapper);
            deserializer.addTrustedPackages("foodcards.adapter.dto");
            return deserializer;
        });
        factory.setKeyDeserializerSupplier(StringDeserializer::new);

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaReceiveDTO> kafkaListener(ConsumerFactory<String, KafkaReceiveDTO> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, KafkaReceiveDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        factory.getContainerProperties().setAckMode(org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL);
        return factory;
    }

}

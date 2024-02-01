package com.danservice.gfm;

import com.danservice.gfm.adapter.inbound.kafka.v1.dto.KafkaClientOrderDTO;
import com.danservice.gfm.adapter.outbound.kafka.v1.dto.KafkaStreetOrderDTO;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.danservice.gfm.domain.OrderType.LIMIT;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.UUID.randomUUID;
import static org.apache.commons.collections4.IterableUtils.toList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES;
import static org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps;
import static org.springframework.kafka.test.utils.KafkaTestUtils.getRecords;

@EmbeddedKafka(partitions = 1, topics = {"${dan.topic.street-order}", "${dan.topic.client-order}"})
@SpringBootTest(classes = Application.class)
class IntegrationTest {
    private static final EasyRandom EASY_RANDOM = new EasyRandom();

    @Value("${dan.topic.street-order}")
    private String streetOrdersTopic;
    @Value("${dan.topic.client-order}")
    private String clientOrdersTopic;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    @SneakyThrows
    void shouldHandleClientOrder() {
        KafkaClientOrderDTO clientOrderDTO = KafkaClientOrderDTO.builder()
                .type(LIMIT)
                .id(randomUUID())
                .instrument(randomAlphabetic(15))
                .quantity(EASY_RANDOM.nextInt(1, 100))
                .price(BigDecimal.valueOf(EASY_RANDOM.nextDouble(1.0d, 100.0d))).build();
        kafkaTemplate.send(clientOrdersTopic, clientOrderDTO.getId().toString(), clientOrderDTO).get();

        verifyKafkaStreetOrderProduced(clientOrderDTO);
    }

    private void verifyKafkaStreetOrderProduced(KafkaClientOrderDTO clientOrderDTO) {
        List<ConsumerRecord<String, KafkaStreetOrderDTO>> consumerRecords = consumeFromKafkaStreetOrderTopic();

        assertEquals(1, consumerRecords.size());
        assertThat(consumerRecords.get(0).value())
                .usingRecursiveComparison()
                .isEqualTo(clientOrderDTO);
    }

    private List<ConsumerRecord<String, KafkaStreetOrderDTO>> consumeFromKafkaStreetOrderTopic() {
        Map<String, Object> consumerProps = consumerProps("test-group", "true", embeddedKafkaBroker);
        consumerProps.put(TRUSTED_PACKAGES, "com.danservice.*");
        consumerProps.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        ConsumerFactory<String, KafkaStreetOrderDTO> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        Consumer<String, KafkaStreetOrderDTO> consumer = cf.createConsumer();

        embeddedKafkaBroker
                .consumeFromAnEmbeddedTopic(consumer, streetOrdersTopic);

        return toList(
                getRecords(consumer, Duration.of(5, SECONDS))
                        .records(streetOrdersTopic));
    }
}

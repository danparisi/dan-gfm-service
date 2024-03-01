package com.danservice.gfm;

import com.danservice.gfm.adapter.inbound.kafka.clientorder.v1.dto.KafkaClientOrderDTO;
import com.danservice.gfm.adapter.inbound.kafka.streetorderack.v1.dto.KafkaStreetOrderAckDTO;
import com.danservice.gfm.adapter.outbound.kafka.streetorder.v1.dto.KafkaStreetOrderDTO;
import com.danservice.gfm.adapter.repository.OrderRepository;
import com.danservice.gfm.domain.OrderStatus;
import com.danservice.gfm.model.OrderEntity;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
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
import java.util.UUID;

import static com.danservice.gfm.domain.OrderStatus.*;
import static com.danservice.gfm.domain.OrderType.LIMIT;
import static java.lang.String.format;
import static java.math.RoundingMode.UP;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Comparator.comparing;
import static java.util.UUID.randomUUID;
import static org.apache.commons.collections4.IterableUtils.toList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES;
import static org.springframework.kafka.support.serializer.JsonDeserializer.TYPE_MAPPINGS;
import static org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps;
import static org.springframework.kafka.test.utils.KafkaTestUtils.getRecords;

@EmbeddedKafka(
        partitions = 3,
        brokerProperties = {
                "transaction.state.log.min.isr=1",
                "offsets.topic.replication.factor=1",
                "transaction.state.log.replication.factor=1"},
        topics = {"${dan.topic.client-order}", "${dan.topic.street-order}", "${dan.topic.street-order-ack}"})
@SpringBootTest(classes = Application.class)
class IntegrationTest {
    private static final EasyRandom EASY_RANDOM = new EasyRandom();

    @Value("${dan.topic.street-order}")
    private String streetOrdersTopic;
    @Value("${dan.topic.street-order-ack}")
    private String streetOrderAcksTopic;
    @Value("${dan.topic.client-order}")
    private String clientOrdersTopic;
    @Value("${dan.topic.street-order-execution}")
    private String streetOrderExecutionsTopic;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private KafkaProperties kafkaProperties;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    @SneakyThrows
    void shouldHandleWholeFlow() {
        final KafkaClientOrderDTO clientOrderDTO = aKafkaClientOrderDTO();
        final UUID orderId = clientOrderDTO.getId();
        kafkaTemplate
                .executeInTransaction(t -> t.send(clientOrdersTopic, orderId.toString(), clientOrderDTO)).get();

        // 1. Expecting new client order
        verifyOrderEntityCreated(clientOrderDTO);
        verifyKafkaStreetOrderProduced(clientOrderDTO);

        final KafkaStreetOrderAckDTO kafkaStreetOrderAckDTO = aKafkaStreetOrderAckDTO(orderId);
        kafkaTemplate
                .executeInTransaction(t -> t.send(streetOrderAcksTopic, orderId.toString(), kafkaStreetOrderAckDTO)).get();

        // 2. Expecting street order ack
        verifyOrderEntityUpdatedToStatus(orderId, RECEIVED_BY_FM, 3);

        kafkaTemplate
                .executeInTransaction(t -> t.send(streetOrderExecutionsTopic, orderId.toString(), orderId)).get();

        // 3. Expecting order execution
        verifyOrderEntityUpdatedToStatus(orderId, EXECUTED, 4);
    }

    private static KafkaStreetOrderAckDTO aKafkaStreetOrderAckDTO(UUID orderId) {
        return KafkaStreetOrderAckDTO.builder().id(orderId).streetId(randomUUID()).build();
    }

    private static KafkaClientOrderDTO aKafkaClientOrderDTO() {
        return KafkaClientOrderDTO.builder()
                .type(LIMIT)
                .id(randomUUID())
                .instrument(randomAlphabetic(15))
                .quantity(EASY_RANDOM.nextInt(1, 100))
                .price(BigDecimal.valueOf(EASY_RANDOM.nextDouble(1.0d, 100.0d))).build();
    }


    private void verifyOrderEntityUpdatedToStatus(UUID orderId, OrderStatus status, int statusUpdates) {
        final var orderEntity = await()
                .atMost(Duration.of(5, SECONDS))
                .until(() -> findMandatoryOrder(orderId), order -> order.getCurrentStatus() == status);

        assertEquals(status, orderEntity.getCurrentStatus());
        assertEquals(statusUpdates, orderEntity.getStatusUpdates().size());
        assertNotNull(orderEntity.getStatusUpdates().get(statusUpdates - 1).getCreatedDate());
        assertEquals(status, orderEntity.getStatusUpdates().get(statusUpdates - 1).getStatus());
        assertTrue(orderEntity.getLastModifiedDate().isAfter(orderEntity.getCreatedDate()));
    }

    private void verifyOrderEntityCreated(KafkaClientOrderDTO clientOrderDTO) {
        final var orderId = clientOrderDTO.getId();
        final var orderEntity = await()
                .atMost(Duration.of(10, SECONDS))
                .ignoreException(IllegalArgumentException.class)
                .until(() -> findMandatoryOrder(orderId), order -> order.getCurrentStatus() == SENT_TO_FM);

        assertThat(orderEntity)
                .usingRecursiveComparison()
                .withComparatorForType(comparing(value -> value.setScale(0, UP)), BigDecimal.class)
                .ignoringFields("currentStatus", "statusUpdates", "createdDate", "lastModifiedDate")
                .isEqualTo(clientOrderDTO);

        assertEquals(SENT_TO_FM, orderEntity.getCurrentStatus());
        assertEquals(2, orderEntity.getStatusUpdates().size());
        assertNotNull(orderEntity.getStatusUpdates().get(0).getCreatedDate());
        assertNotNull(orderEntity.getStatusUpdates().get(1).getCreatedDate());
        assertEquals(RECEIVED, orderEntity.getStatusUpdates().get(0).getStatus());
        assertEquals(SENT_TO_FM, orderEntity.getStatusUpdates().get(1).getStatus());
        assertTrue(orderEntity.getLastModifiedDate().isAfter(orderEntity.getCreatedDate()));
    }

    private OrderEntity findMandatoryOrder(UUID orderId) {
        return orderRepository
                .findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(format("Expected order ID [%s] not found", orderId)));
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
        consumerProps.put(TRUSTED_PACKAGES, "*");
        consumerProps.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(TYPE_MAPPINGS, kafkaProperties.getProducer().getProperties().get(TYPE_MAPPINGS));

        ConsumerFactory<String, KafkaStreetOrderDTO> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        Consumer<String, KafkaStreetOrderDTO> consumer = cf.createConsumer();

        embeddedKafkaBroker
                .consumeFromAnEmbeddedTopic(consumer, streetOrdersTopic);

        return toList(
                getRecords(consumer, Duration.of(5, SECONDS))
                        .records(streetOrdersTopic));
    }
}

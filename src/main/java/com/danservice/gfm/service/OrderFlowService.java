package com.danservice.gfm.service;

import com.danservice.gfm.adapter.KafkaStreetOrderMapper;
import com.danservice.gfm.adapter.inbound.kafka.clientorder.v1.dto.KafkaClientOrderDTO;
import com.danservice.gfm.adapter.inbound.kafka.streetorderack.v1.dto.KafkaStreetOrderAckDTO;
import com.danservice.gfm.adapter.outbound.kafka.streetorder.v1.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

import static com.danservice.gfm.domain.OrderStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFlowService {
    private static final Random RANDOM = new Random();

    private final OrderService orderService;
    private final KafkaProducer kafkaProducer;
    private final KafkaStreetOrderMapper kafkaStreetOrderMapper;

    @SneakyThrows
    public void handleClientOrder(KafkaClientOrderDTO clientOrderDTO) {
        log.info("Handling client order [{}]:", clientOrderDTO);

        var orderId = orderService
                .save(clientOrderDTO, RECEIVED).getId();

        kafkaProducer
                .sendStreetOrder(kafkaStreetOrderMapper.map(clientOrderDTO));

        orderService
                .updateStatus(orderId, SENT_TO_FM);
    }

    public void handleStreetOrderAck(KafkaStreetOrderAckDTO streetOrderAckDTO) {
        log.info("Handling street order ack [{}]:", streetOrderAckDTO);

        var orderId = streetOrderAckDTO.getId();

        orderService
                .updateStatus(orderId, RECEIVED_BY_FM);
    }

    public void handleOrderExecution(UUID orderId) {
        log.info("Handling order execution: [{}]", orderId);

        orderService
                .updateStatus(orderId, EXECUTED);
    }
}

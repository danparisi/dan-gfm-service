package com.danservice.gfm.service;

import com.danservice.gfm.adapter.KafkaStreetOrderMapper;
import com.danservice.gfm.adapter.inbound.kafka.clientorder.v1.dto.KafkaClientOrderDTO;
import com.danservice.gfm.adapter.inbound.kafka.streetorderack.v1.dto.KafkaStreetOrderAckDTO;
import com.danservice.gfm.adapter.outbound.kafka.streetorder.v1.KafkaProducer;
import com.danservice.gfm.domain.OrderStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

import static com.danservice.gfm.domain.OrderStatus.*;
import static java.lang.Thread.sleep;

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
        var orderId = orderService
                .save(clientOrderDTO, RECEIVED).getId();

        sleep(RANDOM.nextInt(0, 500));

        kafkaProducer
                .sendStreetOrder(kafkaStreetOrderMapper.map(clientOrderDTO));

        orderService
                .updateStatus(orderId, SENT_TO_FM);
    }

    public void handleStreetOrderAck(KafkaStreetOrderAckDTO streetOrderAckDTO) {
        var orderId = streetOrderAckDTO.getId();

        orderService
                .updateStatus(orderId, RECEIVED_BY_FM);
    }
}

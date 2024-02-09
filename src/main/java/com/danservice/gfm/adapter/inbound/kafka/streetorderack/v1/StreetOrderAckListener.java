package com.danservice.gfm.adapter.inbound.kafka.streetorderack.v1;

import com.danservice.gfm.adapter.inbound.kafka.clientorder.v1.dto.KafkaClientOrderDTO;
import com.danservice.gfm.adapter.inbound.kafka.streetorderack.v1.dto.KafkaStreetOrderAckDTO;
import com.danservice.gfm.service.OrderFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreetOrderAckListener {
    private final OrderFlowService orderFlowService;

    @Transactional("transactionManager")
    @KafkaListener(id = "${dan.topic.street-order-ack}", topics = "${dan.topic.street-order-ack}")
    public void listen(@Payload KafkaStreetOrderAckDTO payload, @Header(name = RECEIVED_KEY) String key) {
        log.info("Received street order ack: Key=[{}], value=[{}]", key, payload);

        orderFlowService
                .handleStreetOrderAck(payload);
    }
}

package com.danservice.gfm.adapter.inbound.kafka.clientorder.v1;

import com.danservice.gfm.adapter.inbound.kafka.clientorder.v1.dto.KafkaClientOrderDTO;
import com.danservice.gfm.service.OrderFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.kafka.support.KafkaHeaders.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientOrderListener {
    private final OrderFlowService orderFlowService;

    @Transactional("transactionManager")
    @KafkaListener(id = "${dan.topic.client-order}", topics = "${dan.topic.client-order}")
    public void listen(@Payload KafkaClientOrderDTO payload, @Header(name = RECEIVED_KEY) String key) {
        log.info("Received client order: Key=[{}], value=[{}]", key, payload);

        orderFlowService
                .handleClientOrder(payload);
    }
}

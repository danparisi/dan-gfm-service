package com.danservice.gfm.adapter.inbound.kafka.orderexecution.v1;

import com.danservice.gfm.service.OrderFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderExecutionListener {
    private final OrderFlowService orderFlowService;

    @Transactional("transactionManager")
    @KafkaListener(id = "${dan.topic.street-order-execution}", topics = "${dan.topic.street-order-execution}")
    public void listen(@Payload UUID orderId, @Header(name = RECEIVED_KEY) String key) {
        log.info("Received order execution: Key=[{}], value=[{}]", key, orderId);

        orderFlowService
                .handleOrderExecution(orderId);
    }
}

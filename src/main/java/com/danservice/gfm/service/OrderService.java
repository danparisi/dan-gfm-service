package com.danservice.gfm.service;

import com.danservice.gfm.adapter.OrderEntityMapper;
import com.danservice.gfm.adapter.inbound.kafka.clientorder.v1.dto.KafkaClientOrderDTO;
import com.danservice.gfm.adapter.repository.OrderRepository;
import com.danservice.gfm.domain.OrderStatus;
import com.danservice.gfm.model.OrderEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static java.lang.String.format;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderEntityMapper orderEntityMapper;

    @Transactional(value = "transactionManager", propagation = REQUIRES_NEW)
    public OrderEntity save(KafkaClientOrderDTO clientOrderDTO, OrderStatus currentStatus) {
        var orderEntity = orderEntityMapper.map(clientOrderDTO, currentStatus);
        orderEntity
                .updateStatus(currentStatus);

        return orderRepository
                .save(orderEntity);
    }

    @Transactional(value = "transactionManager", propagation = REQUIRES_NEW)
    public void updateStatus(UUID orderId, OrderStatus newStatus) {
        OrderEntity orderEntity = findMandatory(orderId);

        orderEntity
                .updateStatus(newStatus);
    }

    private OrderEntity findMandatory(UUID orderId) {
        return orderRepository
                .findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(format("Expected order ID [%s] not found", orderId)));
    }

}

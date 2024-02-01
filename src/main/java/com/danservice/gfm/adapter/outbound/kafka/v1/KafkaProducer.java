package com.danservice.gfm.adapter.outbound.kafka.v1;

import com.danservice.gfm.adapter.outbound.kafka.v1.dto.KafkaStreetOrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    @Value("${dan.topic.street-order}")
    private String streetOrderTopic;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendStreetOrder(KafkaStreetOrderDTO orderDTO) {
        String key = orderDTO.getId().toString();
        kafkaTemplate.send(streetOrderTopic, key, orderDTO);
    }
}

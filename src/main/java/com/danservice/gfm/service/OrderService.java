package com.danservice.gfm.service;

import com.danservice.gfm.adapter.KafkaStreetOrderMapper;
import com.danservice.gfm.adapter.inbound.kafka.v1.dto.KafkaClientOrderDTO;
import com.danservice.gfm.adapter.outbound.kafka.v1.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

import static java.lang.Thread.sleep;
import static java.util.UUID.randomUUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final KafkaProducer kafkaProducer;
    private final KafkaStreetOrderMapper kafkaStreetOrderMapper;

    @SneakyThrows
    public void handleClientOrder(KafkaClientOrderDTO clientorderdto) {
        sleep(new Random().nextInt(0, 500));

        kafkaProducer.sendStreetOrder(
                kafkaStreetOrderMapper.map(clientorderdto));
    }
}

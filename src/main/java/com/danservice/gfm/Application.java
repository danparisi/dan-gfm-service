package com.danservice.gfm;

import com.danservice.gfm.adapter.outbound.kafka.streetorder.v1.dto.KafkaStreetOrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;

@Slf4j
@EnableKafka
@EnableJpaAuditing
@EnableFeignClients
@SpringBootApplication
@RegisterReflectionForBinding(KafkaStreetOrderDTO.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication
                .run(Application.class, args);
    }

}

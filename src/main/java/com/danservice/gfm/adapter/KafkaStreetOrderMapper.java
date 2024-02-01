package com.danservice.gfm.adapter;


import com.danservice.gfm.adapter.inbound.kafka.v1.dto.KafkaClientOrderDTO;
import com.danservice.gfm.adapter.outbound.kafka.v1.dto.KafkaStreetOrderDTO;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface KafkaStreetOrderMapper {

    KafkaStreetOrderDTO map(KafkaClientOrderDTO apiOrderDTO);

}

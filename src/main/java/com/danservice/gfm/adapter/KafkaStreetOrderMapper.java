package com.danservice.gfm.adapter;


import com.danservice.gfm.adapter.inbound.kafka.clientorder.v1.dto.KafkaClientOrderDTO;
import com.danservice.gfm.adapter.outbound.kafka.streetorder.v1.dto.KafkaStreetOrderDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface KafkaStreetOrderMapper {

    KafkaStreetOrderDTO map(KafkaClientOrderDTO apiOrderDTO);

}

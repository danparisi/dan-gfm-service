package com.danservice.gfm.adapter;


import com.danservice.gfm.adapter.inbound.kafka.clientorder.v1.dto.KafkaClientOrderDTO;
import com.danservice.gfm.domain.OrderStatus;
import com.danservice.gfm.model.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import static org.mapstruct.NullValueMappingStrategy.*;
import static org.mapstruct.NullValuePropertyMappingStrategy.*;

@Mapper(
        componentModel = "spring",
        nullValueMappingStrategy = RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = SET_TO_DEFAULT)
public interface OrderEntityMapper {

    OrderEntity map(KafkaClientOrderDTO kafkaClientOrderDTO, OrderStatus currentStatus);

}

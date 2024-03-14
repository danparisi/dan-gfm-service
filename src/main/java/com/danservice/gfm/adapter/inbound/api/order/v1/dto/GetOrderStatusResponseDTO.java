package com.danservice.gfm.adapter.inbound.api.order.v1.dto;

import com.danservice.gfm.domain.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

@Data
@JsonInclude(NON_NULL)
@Builder(toBuilder = true)
@JsonNaming(SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetOrderStatusResponseDTO {

    private OrderStatus currentStatus;

}
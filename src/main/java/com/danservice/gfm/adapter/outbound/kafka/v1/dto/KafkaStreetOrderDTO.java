package com.danservice.gfm.adapter.outbound.kafka.v1.dto;

import com.danservice.gfm.domain.OrderType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaStreetOrderDTO {

    @NotNull
    private UUID id;
    @NotNull
    private OrderType type;

    @Min(0)
    @NotNull
    private int quantity;
    @NotNull
    @DecimalMin("0.0001")
    private BigDecimal price;
    @NotEmpty
    @Size(min = 1, max = 20)
    private String instrument;

}

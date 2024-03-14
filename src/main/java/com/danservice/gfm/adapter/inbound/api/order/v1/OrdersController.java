package com.danservice.gfm.adapter.inbound.api.order.v1;

import com.danservice.gfm.adapter.inbound.api.order.v1.dto.GetOrderStatusResponseDTO;
import com.danservice.gfm.service.OrderService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.danservice.gfm.adapter.inbound.api.order.v1.OrdersController.BASE_ENDPOINT_ORDERS;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(BASE_ENDPOINT_ORDERS)
public class OrdersController {
    public static final String BASE_ENDPOINT_ORDERS = "/orders/v1";

    private final OrderService orderService;

    @GetMapping("/{orderId}/status")
    public ResponseEntity<GetOrderStatusResponseDTO> getStatus(@NotNull @PathVariable UUID orderId) {
        var orderCurrentStatus = orderService.getOrderCurrentStatus(orderId);
        var response = GetOrderStatusResponseDTO.builder().currentStatus(orderCurrentStatus).build();

        log.info("Returning current order status [{}] for order [{}]", response, orderId);

        return ok(response);
    }
}

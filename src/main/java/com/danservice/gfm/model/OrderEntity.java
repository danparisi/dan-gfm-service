package com.danservice.gfm.model;

import com.danservice.gfm.domain.OrderStatus;
import com.danservice.gfm.domain.OrderType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.Builder.Default;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderEntity {
    @Id
    private UUID id;

    @NotNull
    @Enumerated(STRING)
    private OrderType type;

    @Min(0)
    @NotNull
    private int quantity;

    @Min(0)
    @NotNull
    private BigDecimal price;

    @NotBlank
    private String instrument;

    @NotNull
    @Enumerated(STRING)
    private OrderStatus currentStatus;

    @Default
    @OrderBy("createdDate ASC")
    @OneToMany(fetch = EAGER, cascade = ALL)
    private List<OrderStatusEntity> statusUpdates = new LinkedList<>();

    @NotNull
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @NotNull
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    public void updateStatus(OrderStatus newStatus) {
        setCurrentStatus(newStatus);

        getStatusUpdates()
                .add(OrderStatusEntity.builder()
                        .status(newStatus).build());
    }
}

package com.danservice.gfm.model;

import com.danservice.gfm.domain.OrderStatus;
import com.danservice.gfm.domain.OrderType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Enumerated(STRING)
    private OrderStatus status;

    @NotNull
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;
}

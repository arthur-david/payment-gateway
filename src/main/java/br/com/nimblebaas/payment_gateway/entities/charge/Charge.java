package br.com.nimblebaas.payment_gateway.entities.charge;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.charge.ChargeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "charges")
public class Charge {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String identifier;

    @ManyToOne
    @JoinColumn(name = "originator_user_id", nullable = false)
    private User originatorUser;

    @ManyToOne
    @JoinColumn(name = "destination_user_id", nullable = false)
    private User destinationUser;

    @Column(nullable = false)
    private BigDecimal amount;

    private String description;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChargeStatus status;

    private String errorMessage;

    @OneToOne(mappedBy = "charge", fetch = FetchType.LAZY)
    private ChargePayment payment;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

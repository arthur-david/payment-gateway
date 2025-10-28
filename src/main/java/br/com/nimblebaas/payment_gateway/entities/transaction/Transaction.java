package br.com.nimblebaas.payment_gateway.entities.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.com.nimblebaas.payment_gateway.entities.account.Account;
import br.com.nimblebaas.payment_gateway.entities.account.HoldBalance;
import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import br.com.nimblebaas.payment_gateway.enums.transaction.TransactionPurpose;
import br.com.nimblebaas.payment_gateway.enums.transaction.TransactionStatus;
import br.com.nimblebaas.payment_gateway.enums.transaction.TransactionType;
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
@Table(name = "transactions")
public class Transaction {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "originator_account_id")
    private Account originatorAccount;

    @ManyToOne
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionPurpose purpose;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(nullable = false)
    private String authorizationIdentifier;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hold_balance_id")
    private HoldBalance holdBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_id")
    private Charge charge;

    private String errorMessage;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

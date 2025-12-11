package com.lms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "payment_invoices")
public class PaymentInvoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Stripe Identifiers
    @Column(name = "stripe_invoice_id")
    private String stripeInvoiceId;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    // User linked to this invoice
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Subscription plan someone paid for
    @ManyToOne
    @JoinColumn(name = "subscription_plan_id")
    private SubscriptionPlan subscriptionPlan;

    // Amount charged
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // Invoice status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status; // PAID, UNPAID, FAILED, REFUNDED

    // When invoice was generated
    private LocalDateTime invoiceDate;
}

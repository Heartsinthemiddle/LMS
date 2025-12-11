package com.lms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscription extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    @ManyToOne
    private SubscriptionPlan plan;

    private String stripeCustomerId;
    private String stripeSubscriptionId;

    private LocalDate startDate;
    private LocalDate endDate;

    private String status; // ACTIVE, EXPIRED, CANCELLED
}

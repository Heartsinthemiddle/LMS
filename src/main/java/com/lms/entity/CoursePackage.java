package com.lms.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
public class CoursePackage extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500) // increase length if needed
    private String packageName;

    @Column(length = 500) // for longer descriptions
    private String description;

    private Double monthlyPrice;
    private Double yearlyPrice;

    @Column(length = 500)
    private String stripeMonthlyPriceId;

    @Column(length = 500)
    private String stripeYearlyPriceId;

    private Integer childLimit; // remove courseLimit if not needed
    private Integer courseLimit;

    private Boolean isActive;
}


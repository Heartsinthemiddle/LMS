package com.lms.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
public class CoursePackage extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String packageName;
    private String description;

    private Double monthlyPrice;
    private Double yearlyPrice;

    private String stripeMonthlyPriceId;
    private String stripeYearlyPriceId;

    private Integer childLimit;
    private Integer courseLimit;

    private Boolean isActive;
}

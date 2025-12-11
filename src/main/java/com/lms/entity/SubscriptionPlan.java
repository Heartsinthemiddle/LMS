package com.lms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class SubscriptionPlan extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    private Double monthlyPrice;
    private Double yearlyPrice;

    private Integer userLimit;
    private Integer childLimit;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "course_package_id", referencedColumnName = "id")
    private CoursePackage coursePackage;
}

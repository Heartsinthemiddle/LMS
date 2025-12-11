package com.lms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Child extends BaseEntity{
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private Long externalChildId;

    private String name;
    private String userName;
    private String caseNumber;
    private String gender;

    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;


}

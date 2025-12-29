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
public class Parent extends BaseEntity{
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private Long externalParentId;
    private String name;
    private String userName;
    private String userEmail;
    private String gender;
    @Enumerated(jakarta.persistence.EnumType.STRING)
    private ParentType parentType;

    @OneToOne(mappedBy = "parent", cascade = CascadeType.ALL)
    private User userAccount;


}

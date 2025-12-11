package com.lms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permission")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Permissions extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;  // e.g. "USER_READ", "USER_WRITE"

    private String description;
}


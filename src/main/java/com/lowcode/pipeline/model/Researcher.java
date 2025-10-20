package com.lowcode.pipeline.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Researcher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true)
    private String userName;
    @Column(unique = true)
    private String email;
    String domain;
}

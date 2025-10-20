package com.lowcode.pipeline.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Pipeline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String currentStage;
    private String name;
    private String problemDefinition;

    @ManyToOne
    @JoinColumn
    private Researcher researcher;


}

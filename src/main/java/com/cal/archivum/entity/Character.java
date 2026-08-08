package com.cal.archivum.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "Characters")
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_id" ,nullable = false)
    private Long characterId;

    @Column(name = "character_name" , nullable = false)
    private String characterName;

    @Column(name = "character_species" , nullable = false)
    private String characterSpecies;

    @Column(name = "character_age" , nullable = false)
    private int age;

    @Column(name = "character_desc")
    private String characterDescription;

    @Column(name = "character_nationality")
    private String characterNationality;

    @Column(name = "created_at" , nullable = false)
    private Instant createdAt;

    @JoinColumn(name = "world_id" , nullable = false)
    @ManyToOne
    private World world;

    @PrePersist
    protected void setTime() {
        createdAt = Instant.now();
    }

}

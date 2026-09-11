package com.cal.archivum.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Entity
@Getter @Setter
@Table(name = "worlds")
public class World {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "world_id" ,nullable = false)
    private Long worldId;

    @Column(name = "world_name" ,nullable = false)
    private String worldName;

    @Column(name = "world_desc")
    private String worldDesc;


    @Column(name = "created_at")
    private Instant createdAt;


    @PrePersist
    protected void setTime() {
        createdAt = Instant.now();
    }

}

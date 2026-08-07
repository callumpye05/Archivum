package com.cal.archivum.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Entity
@Getter @Setter
@Table(name = "Worlds")
public class World {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "world_id" , nullable = false)
    private Long world_id;

    @Column(name = "world_name" , nullable = false)
    private String world_name;

    @Column(name = "world_desc")
    private String world_desc;

    @Column(name = "created_at")
    private Instant created_at;

}

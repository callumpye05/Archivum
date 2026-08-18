package com.cal.archivum.entity;

import com.cal.archivum.enums.LocationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter @Setter
@Table(name="locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id" , nullable = false)
    private  Long id;

    @Column(name="location_name" , nullable = false)
    private String locationName;

    @Enumerated(EnumType.STRING)
    @Column(name="location_type" , nullable = false)
    private LocationType locationType;

    @Column(name="location_desc")
    private String locationDescription;

    @Column(name="created_at" , nullable = false)
    private Instant createdAt;

    @JoinColumn(name = "world_id" , nullable = false)
    @ManyToOne
    private World world;

    @PrePersist
    protected void setTime() {
        createdAt = Instant.now();
    }
}

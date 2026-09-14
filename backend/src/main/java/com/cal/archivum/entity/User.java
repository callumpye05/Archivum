package com.cal.archivum.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

@Entity
@Getter @Setter
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id" , nullable = false)
    private  Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name="user_name" , nullable = false , unique = true)
    private String userName;


    @Column(name="password_hash" , nullable = false)
    private String userHashedPassword;

    @Column(name="created_at" , nullable = false)
    private Instant createdAt;


    @PrePersist
    protected void setTime() {
        createdAt = Instant.now();
    }


}

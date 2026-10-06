package com.cal.archivum.entity;


import jakarta.persistence.*;


import java.time.Instant;

@Entity
@Table(name="email_verification_tokens")
public class EmailVerificationToken {

    @Id
    @GeneratedValue
    @Column(name= "token_id" , nullable = false)
    private int id;


    @Column(name = "expiration_date" , nullable = false)
    private Instant expirationDate;

    @Column(name ="hashed_token" , nullable= false)
    private String hashedToken;

    @JoinColumn(name = "user_id" , nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    protected EmailVerificationToken()
    {

    }

    public EmailVerificationToken(Instant expirationDate, String hashedToken, User user) {
        this.expirationDate = expirationDate;
        this.hashedToken = hashedToken;
        this.user = user;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public String getHashedToken() {
        return hashedToken;
    }

    public User getUser() {
        return user;
    }
}



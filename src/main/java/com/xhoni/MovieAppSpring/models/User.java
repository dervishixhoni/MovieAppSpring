package com.xhoni.MovieAppSpring.models;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotEmpty()
    @Size(min = 3)
    private String firstName;
    @NotEmpty()
    @Size(min = 3)
    private String lastName;
    @NotEmpty()
    @Email
    private String email;
    @NotEmpty()
    @Size(min = 8)
    private String password;

    @Transient
    private String passwordConfirmation;

    @Column(columnDefinition = "boolean default false")
    private Boolean isVerified;

    private String verificationCode;

    @Column(updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createAt;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date updateAt;

    @PrePersist
    protected void onCreate() {
        this.createAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateAt = new Date();
    }

    @OneToMany(mappedBy = "addedMovieToWatchlist", fetch = FetchType.LAZY)
    private List<Watchlist> moviesWatchlist;

    public User() {
    }

    public User(Long id, String firstName, String lastName, String email, String password, String passwordConfirmation, Date createAt, Date updateAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.passwordConfirmation = passwordConfirmation;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean value) {
        this.isVerified = value;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String VerificationCode) {
        this.verificationCode = VerificationCode;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }
}

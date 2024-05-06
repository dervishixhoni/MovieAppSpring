package com.xhoni.MovieAppSpring.models;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "watchlists")
public class Watchlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User addedMovieToWatchlist;

    @NotNull
    private float rating;

    @NotNull
    private Long movieId;

    @NotNull
    @DateTimeFormat(pattern = "yyyy")
    private Date releaseYear;


    @Column(updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createAt;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date updateAt;

    @PrePersist
    protected void onCreate(){
        this.createAt = new Date();
    }
    @PreUpdate
    protected void onUpdate(){
        this.updateAt = new Date();
    }

    public Watchlist(Long id, float rating, Long movieId, Date releaseYear, User addedMovieToWatchlist, Date createAt, Date updateAt) {
        this.id = id;
        this.rating = rating;
        this.movieId = movieId;
        this.releaseYear = releaseYear;
        this.addedMovieToWatchlist = addedMovieToWatchlist;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public Watchlist() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public Date getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Date releaseYear) {
        this.releaseYear = releaseYear;
    }

    public User getAddedMovieToWatchlist() {
        return addedMovieToWatchlist;
    }

    public void setAddedMovieToWatchlist(User addedMovieToWatchlist) {
        this.addedMovieToWatchlist = addedMovieToWatchlist;
    }
}

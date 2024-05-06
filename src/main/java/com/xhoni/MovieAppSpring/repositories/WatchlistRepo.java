package com.xhoni.MovieAppSpring.repositories;

import com.xhoni.MovieAppSpring.models.User;
import com.xhoni.MovieAppSpring.models.Watchlist;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchlistRepo extends CrudRepository<Watchlist, Long> {
    List<Watchlist> findAll();
    List<Watchlist> findAllByAddedMovieToWatchlist(User user);

    @Query("SELECT w FROM Watchlist w WHERE w.addedMovieToWatchlist = :user AND w.movieId = :movieId")
    Watchlist findByUserAndMovieId(User user, Long movieId);

}
package com.xhoni.MovieAppSpring.services;

import com.xhoni.MovieAppSpring.models.User;
import com.xhoni.MovieAppSpring.models.Watchlist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xhoni.MovieAppSpring.repositories.WatchlistRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WatchlistService {

    @Autowired
    private WatchlistRepo watchlistRepo;

    public WatchlistService(WatchlistRepo WatchlistRepo) {
        this.watchlistRepo = WatchlistRepo;
    }

    //find all
    public List<Watchlist> getAllWatchlist() {
        return watchlistRepo.findAll();
    }

    public Watchlist getWatchlistById(Long id) {
        Optional<Watchlist> optional = watchlistRepo.findById(id);
        return optional.orElse(null);

    }

    public List<Watchlist> getByUser(User user) {

        return watchlistRepo.findAllByAddedMovieToWatchlist(user);
    }

    public Watchlist createWatchlist(Watchlist watchlist, User user) {
        Watchlist createdWatchlist = new Watchlist();
        createdWatchlist.setMovieId(watchlist.getMovieId());
        createdWatchlist.setAddedMovieToWatchlist(user);
        createdWatchlist.setTitle(watchlist.getTitle());
        createdWatchlist.setRating(watchlist.getRating());
        createdWatchlist.setReleaseYear(watchlist.getReleaseYear());
        return watchlistRepo.save(createdWatchlist);
    }

    public void deleteWatchlist(Watchlist Watchlist) {
        watchlistRepo.delete(Watchlist);
    }

    public ArrayList<Integer> getWatchlistIdsByUser(User user) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (Watchlist watchlist : getByUser(user)) {
            ids.add(Integer.valueOf(watchlist.getMovieId().toString()));
        }
        return ids;
    }

    public Watchlist getWatchlistByUserAndMovieId(User user, Long movieId) {
        return watchlistRepo.findByUserAndMovieId(user, movieId);
    }

}
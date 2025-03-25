package com.example.Repository;

import com.example.Model.Stock;
import com.example.Model.User;
import com.example.Model.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    List<Watchlist> findByUser(User user);

    List<Watchlist> findByUserId(Long userId);

    Optional<Watchlist> findByIdAndUserId(Long id, Long userId);

    boolean existsByNameAndUserId(String name, Long userId);

    @Query("SELECT s FROM Stock s " +
            "JOIN s.watchlists w " +
            "WHERE w.id = :watchlistId " +
            "AND w.user.id = :userId " +
            "AND s.symbol = :stockSymbol")
    Stock getStocksFromWatchList(
            @Param("watchlistId") Long watchlistId,
            @Param("userId") Long userId,
            @Param("stockSymbol") String stockSymbol
    );

}

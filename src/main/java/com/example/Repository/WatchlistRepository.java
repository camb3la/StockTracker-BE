package com.example.Repository;

import com.example.Model.User;
import com.example.Model.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    List<Watchlist> findByUser(User user);

    List<Watchlist> findByUserId(Long userId);

    Optional<Watchlist> findByIdAndUserId(Long id, Long userId);

    boolean existByNameAndUserId(String name, Long userId);

}

package com.example.Controller;

import com.example.Model.User;
import com.example.Model.Watchlist;
import com.example.Repository.UserRepository;
import com.example.Service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/watchlists")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final UserRepository userRepository;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Utente autenticato non trovato nel database"));

        return user.getId();
    }

    @GetMapping
    public ResponseEntity<List<Watchlist>> getWatchlists(){
        Long userID = getCurrentUserId();
        List<Watchlist> watchlists = watchlistService.getWatchlistByUserId(userID);
        return ResponseEntity.ok(watchlists);
    }

    @GetMapping("/{watchlistId}")
    public ResponseEntity<Watchlist> getWatchlist(@PathVariable Long watchlistId) {
        Long userId = getCurrentUserId();
        return watchlistService.getWatchListByIdAndUserId(watchlistId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Watchlist> createWatchlist(@RequestBody Map<String, String> request) {
        Long userId = getCurrentUserId();
        String name = request.get("name");
        String description = request.getOrDefault("description", "");

        Watchlist watchlist = watchlistService.createWatchList(userId, name, description);
        return ResponseEntity.ok(watchlist);
    }

    @DeleteMapping("/{watchlistId}")
    public ResponseEntity<Void> deleteWatchlist(@PathVariable Long watchlistId) {
        Long userId = getCurrentUserId();
        try {
            watchlistService.deleteWatchlist(watchlistId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{watchlistId}/stocks")
    public ResponseEntity<?> addStockToWatchlist(
            @PathVariable Long watchlistId,
            @RequestBody Map<String, String> request) {
        Long userId = getCurrentUserId();
        String symbol = request.get("symbol");

        try {
            Watchlist watchlist = watchlistService.addStockToWatchlist(watchlistId, userId, symbol);
            return ResponseEntity.ok(watchlist);
        } catch (IllegalArgumentException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("già presente")) {
                return ResponseEntity.status(409)
                        .body(Map.of("error", errorMessage));
            }
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Impossibile aggiungere l'azione. Verifica che il simbolo sia corretto."));
        }
    }

    @DeleteMapping("/{watchlistId}/stocks/{symbol}")
    public ResponseEntity<Watchlist> removeStockFromWatchlist(
            @PathVariable Long watchlistId,
            @PathVariable String symbol) {
        Long userId = getCurrentUserId();
        try {
            Watchlist watchlist = watchlistService.removeStockFromWatchlist(watchlistId, userId, symbol);
            return ResponseEntity.ok(watchlist);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}

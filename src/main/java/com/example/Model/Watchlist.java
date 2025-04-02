package com.example.Model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.HashSet;


@Entity
@Table(name = "watchlist")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "stocks")
@ToString(exclude = "stocks")
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "watchlist_stocks",
            joinColumns = @JoinColumn(name = "watchlist_id"),
            inverseJoinColumns = @JoinColumn(name = "stock_symbol")
    )
    private Set<Stock> stocks = new HashSet<>();

    public void addStock(Stock stock) {
        this.stocks.add(stock);
        stock.getWatchlists().add(this);
    }

    public void removeStock(Stock stock) {
        this.stocks.remove(stock);
        stock.getWatchlists().remove(this);
    }
}
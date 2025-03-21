package com.example.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "stocks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Stock {

    @Id
    @Column(nullable = false, unique = true)
    private String symbol;

    @Column(nullable = false)
    private String name;

    @Column(name = "current_price")
    private BigDecimal currentPrice;

    @Column(name = "daily_change")
    private BigDecimal dailyChange;

    @Column(name = "daily_change_percent")
    private BigDecimal dailyChangePercent;

    @Column(name = "market_cap")
    private BigDecimal marketCap;

    private BigDecimal volume;

    @Column(name = "exchange_name")
    private String exchangeName;

    @Column(name = "currency")
    private String currency;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @ManyToMany(mappedBy = "stocks")
    private Set<Watchlist> watchlists = new HashSet<>();

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PriceAnalysis> priceAnalyses = new HashSet<>();

}

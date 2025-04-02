package com.example.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "watchlists")
@ToString(exclude = "watchlists")
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

    @JsonIgnore
    @ManyToMany(mappedBy = "stocks")
    private Set<Watchlist> watchlists = new HashSet<>();

}

package com.example.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "price_analysis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_price", nullable = false)
    private BigDecimal targetPrice;

    @Column(name = "position_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PositionType positionType;

    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_symbol", nullable = false)
    private Stock stock;

    public enum PositionType {
        LONG, SHORT
    }

}

package com.example.Controller;

import com.example.Model.Stock;
import com.example.Service.AlphaVantageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
@Tag(name = "Stock", description = "Api per la gestione delle azioni")
@CrossOrigin(origins = "http://localhost:4200")
public class StockController {

    private final AlphaVantageService alphaVantageService;

    @GetMapping("/search")
    @Operation(summary = "Cerca Azioni per nome o simbolo")
    public ResponseEntity<List<Stock>> searchStock(@RequestParam String query){
        List<Stock> stocks = alphaVantageService.searchStock(query);
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/details/{symbol}")
    @Operation(summary = "Ottieni dettagli di un'azione per simbolo")
    public ResponseEntity<Stock> getStockDetails(@PathVariable String symbol){
        Stock stock = alphaVantageService.getStockDetails(symbol);
        return ResponseEntity.ok(stock);
    }

}

package com.example.Controller;

import com.example.Exception.AlphaVantageException;
import com.example.Model.Stock;
import com.example.Service.AlphaVantageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
@Tag(name = "Stock", description = "Api per la gestione delle azioni")
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class StockController {

    private final AlphaVantageService alphaVantageService;

    @GetMapping("/details/{symbol}")
    @Operation(summary = "Ottieni dettagli di un'azione per simbolo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dettagli dell'azione recuperati con successo"),
            @ApiResponse(responseCode = "404", description = "Simbolo non trovato"),
            @ApiResponse(responseCode = "429", description = "Limite di chiamate API superato"),
            @ApiResponse(responseCode = "503", description = "Servizio Alpha Vantage non disponibile"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    public ResponseEntity<?> getStockDetails(@PathVariable String symbol) {
        try {
            Stock stock = alphaVantageService.getStockDetails(symbol);
            return ResponseEntity.ok(stock);
        } catch (AlphaVantageException ex) {
            log.warn("Errore Alpha Vantage durante il recupero del simbolo {}: {}", symbol, ex.getMessage());
            throw ex;
        }
    }
}
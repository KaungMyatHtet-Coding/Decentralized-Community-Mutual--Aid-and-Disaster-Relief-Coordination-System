package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.Stock;
import com.hnaungkyoe.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "*")
public class StockController {
    private final StockService service;

    @Autowired
    public StockController(StockService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Stock> addOrUpdate(@RequestBody Stock stock) {
        return ResponseEntity.ok(service.addOrUpdateStock(stock));
    }

    @GetMapping
    public ResponseEntity<List<Stock>> getAll() {
        return ResponseEntity.ok(service.getAllStocks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stock> getById(@PathVariable Long id) {
        return service.getStockById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
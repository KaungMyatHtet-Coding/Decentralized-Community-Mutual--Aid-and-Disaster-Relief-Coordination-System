package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.Stock;
import com.hnaungkyoe.repository.StockRepository;
import com.hnaungkyoe.entity.EmergencyUsageLog;
import com.hnaungkyoe.repository.EmergencyUsageLogRepository;
import com.hnaungkyoe.dto.EmergencyUsageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class StockService {
    private final StockRepository stockRepository;
    private final EmergencyUsageLogRepository logRepository;

    @Autowired
    public StockService(StockRepository stockRepository, EmergencyUsageLogRepository logRepository) {
        this.stockRepository = stockRepository;
        this.logRepository = logRepository;
    }

    public Stock addOrUpdateStock(Stock stock) {
        if (stock.getTownship() == null) {
            stock.setTownship("Central HQ"); // Fallback
        }
        Optional<Stock> existing = stockRepository.findByItemNameAndTownship(stock.getItemName(), stock.getTownship());
        if (existing.isPresent()) {
            Stock e = existing.get();
            e.setQuantity(e.getQuantity() + stock.getQuantity());
            return stockRepository.save(e);
        }
        return stockRepository.save(stock);
    }

    public Stock recordEmergencyUsage(EmergencyUsageRequest request, String authorizedBy) {
        Optional<Stock> existing = stockRepository.findByItemNameAndTownship(request.getItemName(), request.getTownship());
        if (existing.isPresent()) {
            Stock e = existing.get();
            double newQuantity = e.getQuantity() - request.getQuantityUsed();
            e.setQuantity(newQuantity < 0 ? 0 : newQuantity);
            Stock savedStock = stockRepository.save(e);
            
            EmergencyUsageLog log = EmergencyUsageLog.builder()
                .itemName(request.getItemName())
                .township(request.getTownship())
                .quantityUsed(request.getQuantityUsed())
                .reasonDetails(request.getReasonDetails())
                .proofPhotoUrl(request.getProofPhotoUrl())
                .usageDate(request.getUsageDate() != null ? request.getUsageDate() : java.time.LocalDateTime.now())
                .authorizedBy(authorizedBy)
                .build();
            logRepository.save(log);
            
            return savedStock;
        }
        throw new RuntimeException("Stock not found for item: " + request.getItemName() + " in township: " + request.getTownship());
    }

    public List<EmergencyUsageLog> getEmergencyLogs(String township) {
        if (township != null && !township.isEmpty()) {
            return logRepository.findByTownshipOrderByUsageDateDesc(township);
        }
        return logRepository.findAllByOrderByUsageDateDesc();
    }

    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    public List<Stock> getStocksByTownship(String township) {
        return stockRepository.findByTownship(township);
    }

    public Optional<Stock> getStockById(Long id) {
        return stockRepository.findById(id);
    }
}
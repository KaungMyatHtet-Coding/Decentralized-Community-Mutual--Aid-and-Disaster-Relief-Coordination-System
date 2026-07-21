package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByItemNameAndTownship(String itemName, String township);
    Optional<Stock> findFirstByItemName(String itemName);
    List<Stock> findByTownship(String township);
    List<Stock> findByCategory(Stock.Category category);

    @Query("SELECT s FROM Stock s WHERE s.quantity < :threshold")
    List<Stock> findLowStockItems(@Param("threshold") Double threshold);

    
}
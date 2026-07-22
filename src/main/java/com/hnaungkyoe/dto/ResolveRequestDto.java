package com.hnaungkyoe.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResolveRequestDto {
    private String proofPhotoUrl;
    private List<StockDeduction> deductions;

    @Data
    public static class StockDeduction {
        private Long stockId;
        private Double quantity;
    }
}

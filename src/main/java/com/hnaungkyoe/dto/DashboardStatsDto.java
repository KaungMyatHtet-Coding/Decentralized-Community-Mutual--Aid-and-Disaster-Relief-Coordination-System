package com.hnaungkyoe.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardStatsDto {
    private Double totalMoneyReceived;
    private Double totalMoneyDistributed;

    // 📊 Chart ထဲမှာပြဖို့ Category အလိုက် လက်ကျန်စာရင်း
    private List<CategoryStat> categoryStats;

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class CategoryStat {
        private String category;
        private Double received;
        private Double distributed;
        private Double available;
    }
}
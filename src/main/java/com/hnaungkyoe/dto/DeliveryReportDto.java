package com.hnaungkyoe.dto;

import lombok.Data;
import java.util.List;

/** DTO from Volunteer when submitting a delivery report */
@Data
public class DeliveryReportDto {
    private Long aidRequestId;
    private String proofPhotoUrl;
    private String notes;
    private List<DeliveredItemEntryDto> deliveredItems;

    @Data
    public static class DeliveredItemEntryDto {
        private String itemName;
        private Double quantityDelivered;
        private String unit;
    }
}

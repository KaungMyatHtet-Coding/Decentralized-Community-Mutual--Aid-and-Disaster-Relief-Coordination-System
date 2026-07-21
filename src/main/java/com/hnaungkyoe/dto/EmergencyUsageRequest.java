package com.hnaungkyoe.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class EmergencyUsageRequest {
    private String itemName;
    private String township;
    private Double quantityUsed;
    private String reasonDetails;
    private String proofPhotoUrl;
    private LocalDateTime usageDate;
}

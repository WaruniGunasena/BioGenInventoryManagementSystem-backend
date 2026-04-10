package com.biogenholdings.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReturnItemDetailDTO {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
    private String reason;
    private Boolean isReusable; // To show "Restocked" vs "Discarded/For Reissue" on invoice
    private String units;
}

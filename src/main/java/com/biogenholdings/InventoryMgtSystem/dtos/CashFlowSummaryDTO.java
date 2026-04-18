package com.biogenholdings.InventoryMgtSystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CashFlowSummaryDTO {

    private BigDecimal netCashInflow;
    private long totalSalesCount;
    private double inflowPercentageChange;

    private BigDecimal netCashOutflow;
    private long totalGrnCount;
    private double outflowPercentageChange;

    private BigDecimal operatingCashFlow;

    private BigDecimal accountsReceivable;
    private Long pendingSalesCount;

    private BigDecimal accountsPayable;
    private Long pendingPurchaseCount;

}

package com.biogenholdings.InventoryMgtSystem.services;

import com.biogenholdings.InventoryMgtSystem.dtos.Response;

import java.time.LocalDate;

public interface CashFlowService {

    Response getPendingCredits(LocalDate startDate, LocalDate endDate);
    Response getCompletedCashFlow(LocalDate startDate, LocalDate endDate);
}

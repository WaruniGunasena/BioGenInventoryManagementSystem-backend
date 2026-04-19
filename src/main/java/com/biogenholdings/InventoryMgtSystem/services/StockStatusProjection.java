package com.biogenholdings.InventoryMgtSystem.services;

public interface StockStatusProjection {
    Long getOutOfStock();
    Long getLowStock();
    Long getHealthy();
}

package com.biogenholdings.InventoryMgtSystem.services;

import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.dtos.StockResponseDTO;
import com.biogenholdings.InventoryMgtSystem.enums.FilterEnum;

import java.util.List;

public interface StockService {

    List<StockResponseDTO> getAllStocks();

    Response searchStock(String searchKey);

    Response getPaginatedStocks(Integer page, Integer size, FilterEnum filter);
}

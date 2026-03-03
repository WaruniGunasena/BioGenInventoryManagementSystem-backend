package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.dtos.StockResponseDTO;
import com.biogenholdings.InventoryMgtSystem.enums.FilterEnum;
import com.biogenholdings.InventoryMgtSystem.repositories.ProductStockRepository;
import com.biogenholdings.InventoryMgtSystem.services.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final ProductStockRepository productStockRepository;

    @Override
    public List<StockResponseDTO> getAllStocks() {
        return productStockRepository.getAllStockData();
    }

    @Override
    public Response searchStock(String searchKey) {
        List<StockResponseDTO> stockResponseDTOList =
                productStockRepository.searchStockByName(searchKey);

        return Response.builder()
                .status(200)
                .message("Success")
                .productStocks(stockResponseDTOList)
                .build();
    }

    @Override
    public Response getPaginatedStocks(Integer page, Integer size, FilterEnum filter) {

        Sort sort = Sort.by(filter == FilterEnum.DESC ? Sort.Direction.DESC : Sort.Direction.ASC, "p.name");

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<StockResponseDTO> stockPage =
                productStockRepository.getPaginatedStockData(pageable);

        return Response.builder()
                .status(200)
                .message("Success")
                .productStocks(stockPage.getContent())
                .totalPages(stockPage.getTotalPages())
                .totalElements(stockPage.getTotalElements())
                .build();
    }
}

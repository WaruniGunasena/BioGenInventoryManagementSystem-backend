package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.dtos.SalesRepCommissionDTO;
import com.biogenholdings.InventoryMgtSystem.models.SalesCommissionSummary;
import com.biogenholdings.InventoryMgtSystem.repositories.SalesCommissionSummaryRepository;
import com.biogenholdings.InventoryMgtSystem.services.CommissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j

public class CommissionServiceImpl implements CommissionService {

    private final SalesCommissionSummaryRepository salesCommissionSummaryRepository;

    @Override
    public Response getMyCommissions(Long userId) {

        List<SalesCommissionSummary> commissions = salesCommissionSummaryRepository.findBySalesRepId(userId);

        List<SalesRepCommissionDTO> dtoList = commissions.stream()
                .map(c -> SalesRepCommissionDTO.builder()
                        .invoiceNumber(c.getInvoiceNumber())
                        .customer(c.getCustomer().getName()) // Mapping ID to Name
                        .date(c.getInvoiceDate().toLocalDate())
                        .commissionableAmount(c.getCommissionableAmount())
                        .totalCommission(c.getTotalCommission())
                        .build())
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("Commissions retrieved successfully")
                .data(dtoList) // This 'data' field will be used by your React setCommissions(response.data)
                .build();
    }
}

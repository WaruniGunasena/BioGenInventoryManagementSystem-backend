package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.CreditDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.models.GRN;
import com.biogenholdings.InventoryMgtSystem.models.GRNPayment;
import com.biogenholdings.InventoryMgtSystem.repositories.GRNPaymentRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.GRNRepository;
import com.biogenholdings.InventoryMgtSystem.services.CashFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CashFlowServiceImpl implements CashFlowService {

    private final GRNRepository grnRepository;
    private final GRNPaymentRepository grnPaymentRepository;

    @Override
    public Response getPendingCredits(LocalDate startDate, LocalDate endDate) {

        List<GRN> pendingGRNs = grnRepository.findAllPendingCredits();

        List<CreditDTO> credits = pendingGRNs.stream()
                .map(grn -> mapToCreditDTO(grn, startDate, endDate))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return Response.builder()
                .credits(credits)
                .debits(Collections.emptyList())
                .build();
    }

    private CreditDTO mapToCreditDTO(GRN grn, LocalDate startDate, LocalDate endDate) {

        BigDecimal dueAmount;

        LocalDate grnDate = grn.getGrnDate();
        LocalDate dueDate;

        String creditPeriod = grn.getSupplier().getCreditPeriod();

        if (creditPeriod == null || creditPeriod.trim().equalsIgnoreCase("CASH")) {
            dueDate = grnDate;
        } else {
            try {
                int creditDays = Integer.parseInt(creditPeriod.replaceAll("[^0-9]", ""));
                dueDate = grnDate.plusDays(creditDays);
            } catch (Exception e) {
                dueDate = grnDate;
            }
        }

        if ("PARTIAL".equalsIgnoreCase(grn.getPaymentStatus())) {

            GRNPayment latestPayment = grnPaymentRepository
                    .findTopByGrnIdOrderByIdDesc(grn.getId());

            dueAmount = (latestPayment != null)
                    ? latestPayment.getDueBalance()
                    : grn.getGrandTotal();

        } else {
            dueAmount = grn.getGrandTotal();
        }

        if (grnDate.isAfter(endDate) || dueDate.isBefore(startDate)) {
            return null;
        }

        LocalDate displayDate = dueDate;

        return CreditDTO.builder()
                .grnId(grn.getId())
                .supplier(grn.getSupplier().getName())
                .amount(dueAmount)
                .date(displayDate)
                .invoiceNumber(grn.getInvoiceNumber())
                .build();
    }
}
package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.CashFlowSummaryDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.CreditDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.DebitDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.models.GRN;
import com.biogenholdings.InventoryMgtSystem.models.GRNPayment;
import com.biogenholdings.InventoryMgtSystem.models.SalesOrder;
import com.biogenholdings.InventoryMgtSystem.models.SalesOrderPayment;
import com.biogenholdings.InventoryMgtSystem.repositories.GRNPaymentRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.GRNRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.SalesOrderPaymentRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.SalesOrderRepository;
import com.biogenholdings.InventoryMgtSystem.services.CashFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CashFlowServiceImpl implements CashFlowService {

    private final GRNRepository grnRepository;
    private final GRNPaymentRepository grnPaymentRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderPaymentRepository salesOrderPaymentRepository;

    @Override
    public Response getPendingCashFlow(LocalDate startDate, LocalDate endDate) {

        List<GRN> pendingGRNs = grnRepository.findAllPendingCredits();
        List<CreditDTO> credits = pendingGRNs.stream()
                .map(grn -> mapToCreditDTO(grn, startDate, endDate))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<SalesOrder> pendingSales = salesOrderRepository.findAllPendingDebits();
        List<DebitDTO> debits = pendingSales.stream()
                .map(sale -> mapToDebitDTO(sale, startDate, endDate))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return Response.builder()
                .credits(credits)
                .debits(debits)
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

    private DebitDTO mapToDebitDTO(SalesOrder salesOrder, LocalDate startDate, LocalDate endDate) {

        BigDecimal dueAmount;

        LocalDate invoiceDate = salesOrder.getInvoiceDate();
        LocalDate dueDate;

        String creditPeriod = salesOrder.getCustomer().getCreditPeriod();

        if (creditPeriod == null || creditPeriod.trim().equalsIgnoreCase("CASH")) {
            dueDate = invoiceDate;
        } else {
            try {
                int creditDays = Integer.parseInt(creditPeriod.replaceAll("[^0-9]", ""));
                dueDate = invoiceDate.plusDays(creditDays);
            } catch (Exception e) {
                dueDate = invoiceDate;
            }
        }

        if ("PARTIAL".equalsIgnoreCase(salesOrder.getPaymentStatus())) {

            SalesOrderPayment latestPayment = salesOrderPaymentRepository
                    .findTopBySalesOrderIdOrderByIdDesc(salesOrder.getId());

            dueAmount = (latestPayment != null)
                    ? latestPayment.getDueBalance()
                    : salesOrder.getGrandTotal();

        } else {
            dueAmount = salesOrder.getGrandTotal();
        }

        if (invoiceDate.isAfter(endDate) || dueDate.isBefore(startDate)) {
            return null;
        }

        LocalDate displayDate = dueDate;

        return DebitDTO.builder()
                .salesOrderId(salesOrder.getId())
                .customer(salesOrder.getCustomer().getName())
                .invoiceNumber(salesOrder.getInvoiceNumber())
                .amount(dueAmount)
                .date(displayDate)
                .build();
    }

    @Override
    public Response getCompletedCashFlow(LocalDate startDate, LocalDate endDate) {

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<GRNPayment> payments = grnPaymentRepository
                .findCompletedPayments(startDateTime, endDateTime);

        List<CreditDTO> credits = payments.stream()
                .map(payment -> CreditDTO.builder()
                        .grnId(payment.getGrn().getId())
                        .supplier(payment.getGrn().getSupplier().getName())
                        .invoiceNumber(payment.getGrn().getInvoiceNumber())
                        .amount(payment.getAmount())
                        .date(payment.getCreatedAt().toLocalDate())
                        .build()
                )
                .collect(Collectors.toList());

        List<SalesOrderPayment> incomePayments = salesOrderPaymentRepository
                .findCompletedPayments(startDateTime, endDateTime);

        List<DebitDTO> debits = incomePayments.stream()
                .map(payment -> DebitDTO.builder()
                        .salesOrderId(payment.getSalesOrder().getId())
                        .customer(payment.getSalesOrder().getCustomer().getName())
                        .invoiceNumber(payment.getSalesOrder().getInvoiceNumber())
                        .amount(payment.getAmount())
                        .date(payment.getCreatedAt().toLocalDate())
                        .build()
                )
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("Completed cash flow retrieved successfully")
                .credits(credits)
                .debits(debits)
                .build();
    }

    @Override
    public Response getCashFlowSummary(LocalDate startDate, LocalDate endDate) {

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        long daysInPeriod = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDateTime prevStartDateTime = startDateTime.minusDays(daysInPeriod);
        LocalDateTime prevEndDateTime = endDateTime.minusDays(daysInPeriod);

        BigDecimal currentInflow = salesOrderRepository.sumCompletedInflow(startDateTime, endDateTime);
        BigDecimal previousInflow = salesOrderRepository.sumCompletedInflow(prevStartDateTime, prevEndDateTime);

        currentInflow = (currentInflow != null) ? currentInflow : BigDecimal.ZERO;
        previousInflow = (previousInflow != null) ? previousInflow : BigDecimal.ZERO;

        double inflowChange = calculatePercentageChange(currentInflow, previousInflow);

        BigDecimal netInflow = salesOrderRepository.sumCompletedInflow(startDateTime, endDateTime);
        long salesCount = salesOrderRepository.countSalesWithPayments(startDateTime, endDateTime);

        BigDecimal currentOutflow = grnPaymentRepository.sumCompletedOutflow(startDateTime, endDateTime);
        BigDecimal prevOutflow = grnPaymentRepository.sumCompletedOutflow(prevStartDateTime, prevEndDateTime);

        currentOutflow = (currentOutflow != null) ? currentOutflow : BigDecimal.ZERO;
        prevOutflow = (prevOutflow != null) ? prevOutflow : BigDecimal.ZERO;

        double outflowChange = calculatePercentageChange(currentOutflow, prevOutflow);

        BigDecimal netOutflow = grnPaymentRepository.sumCompletedOutflow(startDateTime, endDateTime);
        long grnCount = grnPaymentRepository.countCompletedPayments(startDateTime, endDateTime);

        BigDecimal operatingFlow = currentInflow.subtract(currentOutflow);

        BigDecimal accountsReceivable = salesOrderRepository.calculateTotalAccountsReceivable();
        long pendingSalesCount = salesOrderRepository.countPendingSales();

        BigDecimal accountsPayable = grnRepository.calculateTotalAccountsPayable();
        long pendingPurchaseCount = grnRepository.countPendingPurchases();

        CashFlowSummaryDTO summary = CashFlowSummaryDTO.builder()
                .netCashInflow(netInflow != null ? netInflow : BigDecimal.ZERO)
                .totalSalesCount(salesCount)
                .inflowPercentageChange(inflowChange)
                .netCashOutflow(netOutflow != null ? currentOutflow : BigDecimal.ZERO)
                .outflowPercentageChange(outflowChange)
                .totalGrnCount(grnCount)
                .operatingCashFlow(operatingFlow)
                .accountsReceivable(accountsReceivable)
                .pendingSalesCount(pendingSalesCount)
                .accountsPayable(accountsPayable)
                .pendingPurchaseCount(pendingPurchaseCount)
                .build();

        return Response.builder()
                .status(200)
                .message("Summary retrieved successfully")
                .cashFlowSummary(summary)
                .build();
    }

    private double calculatePercentageChange(BigDecimal current, BigDecimal previous) {

        if (current.compareTo(BigDecimal.ZERO) == 0 && previous.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return 100.0;
        }

        BigDecimal difference = current.subtract(previous);
        return difference.divide(previous, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .doubleValue();
    }
}
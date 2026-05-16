package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.*;
import com.biogenholdings.InventoryMgtSystem.enums.DiscountTypeEnum;
import com.biogenholdings.InventoryMgtSystem.models.*;
import com.biogenholdings.InventoryMgtSystem.repositories.*;
import com.biogenholdings.InventoryMgtSystem.services.CashFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final MonthlyCommissionInvoiceRepository commissionRepository;
    private final CommissionPaymentRepository commissionPaymentRepository;

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

        List<MonthlyCommissionInvoice> pendingCommissions = commissionRepository.findAllPendingCommissions();
        List<CommissionDTO> commissions = pendingCommissions.stream()
                .map(invoice -> mapToCommissionDTO(invoice, startDate, endDate))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return Response.builder()
                .credits(credits)
                .debits(debits)
                .commissions(commissions)
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

        String paymentStatus = salesOrder.getPaymentStatus();
        if ("PARTIAL".equalsIgnoreCase(paymentStatus) || "REALIZING".equalsIgnoreCase(paymentStatus)) {

            SalesOrderPayment latestPayment = salesOrderPaymentRepository
                    .findTopBySalesOrderIdOrderByIdDesc(salesOrder.getId());

            dueAmount = (latestPayment != null)
                    ? latestPayment.getDueBalance()
                    : calculateNetTotal(salesOrder.getGrandTotal(),salesOrder.getCourierCharges(),salesOrder.getAdditionalDiscount(),salesOrder.getReturnCredits(),salesOrder.getAdditionalDiscountType());

        } else {
            dueAmount = calculateNetTotal(salesOrder.getGrandTotal(),salesOrder.getCourierCharges(),salesOrder.getAdditionalDiscount(),salesOrder.getReturnCredits(),salesOrder.getAdditionalDiscountType());
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

    private CommissionDTO mapToCommissionDTO(MonthlyCommissionInvoice invoice, LocalDate startDate, LocalDate endDate) {

        if (invoice.getGeneratedDate() == null) {
            return null;
        }

        LocalDate invoiceDate = invoice.getGeneratedDate().toLocalDate();

        if (invoiceDate.isAfter(endDate) || invoiceDate.isBefore(startDate)) {
            return null;
        }

        String repName = (invoice.getSalesRep() != null) ? invoice.getSalesRep().getName() : "Unknown Rep";

        return CommissionDTO.builder()
                .commission_invoice_number(invoice.getCommissionInvoiceNumber())
                .salesRep(repName)
                .net_payout(invoice.getNetPayout())
                .month_year(invoice.getMonthYear())
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

        List<CommissionPayment> commissionPayments = commissionPaymentRepository
                .findCompletedPayments(startDateTime, endDateTime);

        List<CommissionDTO> commissions = commissionPayments.stream()
                .map(payment -> {
                    var invoice = payment.getInvoice();
                    String repName = (invoice != null && invoice.getSalesRep() != null)
                            ? invoice.getSalesRep().getName()
                            : "Unknown Rep";
                    String invoiceNum = (invoice != null) ? invoice.getCommissionInvoiceNumber() : "N/A";

                    return CommissionDTO.builder()
                            .commission_invoice_number(invoiceNum)
                            .salesRep(repName)
                            .net_payout(payment.getAmount())
                            .paid_date(payment.getCreatedAt().toLocalDate())
                            .build();
                })
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("Completed cash flow retrieved successfully")
                .credits(credits)
                .debits(debits)
                .commissions(commissions)
                .build();
    }

    @Override
    public Response getCashFlowSummary(LocalDate startDate, LocalDate endDate) {

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        long daysInPeriod = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDateTime prevStartDateTime = startDateTime.minusDays(daysInPeriod);
        LocalDateTime prevEndDateTime = endDateTime.minusDays(daysInPeriod);

        // --- INFLOW (Income) ---
        BigDecimal currentInflow = salesOrderRepository.sumCompletedInflow(startDateTime, endDateTime);
        BigDecimal previousInflow = salesOrderRepository.sumCompletedInflow(prevStartDateTime, prevEndDateTime);
        currentInflow = (currentInflow != null) ? currentInflow : BigDecimal.ZERO;
        previousInflow = (previousInflow != null) ? previousInflow : BigDecimal.ZERO;

        double inflowChange = calculatePercentageChange(currentInflow, previousInflow);
        long salesCount = salesOrderRepository.countSalesWithPayments(startDateTime, endDateTime);

        // --- OUTFLOW (Expense: GRN + Commissions) ---
        // Current Period
        BigDecimal currentGrnOutflow = grnPaymentRepository.sumCompletedOutflow(startDateTime, endDateTime);
        BigDecimal currentCommOutflow = commissionPaymentRepository.sumCompletedCommissionPayments(startDateTime, endDateTime);
        BigDecimal totalCurrentOutflow = (currentGrnOutflow != null ? currentGrnOutflow : BigDecimal.ZERO)
                .add(currentCommOutflow != null ? currentCommOutflow : BigDecimal.ZERO);

        // Previous Period
        BigDecimal prevGrnOutflow = grnPaymentRepository.sumCompletedOutflow(prevStartDateTime, prevEndDateTime);
        BigDecimal prevCommOutflow = commissionPaymentRepository.sumCompletedCommissionPayments(prevStartDateTime, prevEndDateTime);
        BigDecimal totalPrevOutflow = (prevGrnOutflow != null ? prevGrnOutflow : BigDecimal.ZERO)
                .add(prevCommOutflow != null ? prevCommOutflow : BigDecimal.ZERO);

        double outflowChange = calculatePercentageChange(totalCurrentOutflow, totalPrevOutflow);

        // Total Expense Counts
        long grnCount = grnPaymentRepository.countCompletedPayments(startDateTime, endDateTime);
        long commCount = commissionPaymentRepository.countCompletedCommissionPayments(startDateTime, endDateTime);

        // Operating Flow (Profit/Loss)
        BigDecimal operatingFlow = currentInflow.subtract(totalCurrentOutflow);

        // --- PENDING PAYMENTS (Accounts Receivable/Payable) ---
        BigDecimal accountsReceivable = salesOrderRepository.calculateTotalAccountsReceivable();
        long pendingSalesCount = salesOrderRepository.countPendingSales();

        // Accounts Payable = Pending GRNs + Pending Commission Invoices
        BigDecimal pendingGrnAmount = grnRepository.calculateTotalAccountsPayable();
        BigDecimal pendingCommAmount = commissionRepository.calculateTotalPendingCommissions();
        BigDecimal totalAccountsPayable = (pendingGrnAmount != null ? pendingGrnAmount : BigDecimal.ZERO)
                .add(pendingCommAmount != null ? pendingCommAmount : BigDecimal.ZERO);

        long pendingPurchaseCount = grnRepository.countPendingPurchases();
        long pendingCommCount = commissionRepository.countPendingCommissions();

        CashFlowSummaryDTO summary = CashFlowSummaryDTO.builder()
                .netCashInflow(currentInflow)
                .totalSalesCount(salesCount)
                .inflowPercentageChange(inflowChange)
                .netCashOutflow(totalCurrentOutflow)
                .outflowPercentageChange(outflowChange)
                .totalGrnCount(grnCount + commCount)
                .operatingCashFlow(operatingFlow)
                .accountsReceivable(accountsReceivable != null ? accountsReceivable : BigDecimal.ZERO)
                .pendingSalesCount(pendingSalesCount)
                .accountsPayable(totalAccountsPayable)
                .pendingPurchaseCount(pendingPurchaseCount + pendingCommCount)
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

    private BigDecimal calculateNetTotal(
            BigDecimal grandTotal,
            BigDecimal courierCharges,
            BigDecimal discountValue,
            BigDecimal returnCredits,
            DiscountTypeEnum discountType
    ) {
        if (grandTotal == null) grandTotal = BigDecimal.ZERO;
        if (courierCharges == null) courierCharges = BigDecimal.ZERO;
        if (discountValue == null) discountValue = BigDecimal.ZERO;
        if (returnCredits == null) returnCredits = BigDecimal.ZERO;

        BigDecimal discountAmount = BigDecimal.ZERO;


        if (discountType == DiscountTypeEnum.cash) {
            discountAmount = discountValue;
        } else if (discountType == DiscountTypeEnum.percentage) {
            discountAmount = grandTotal
                    .multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        return grandTotal
                .add(courierCharges)
                .subtract(discountAmount)
                .subtract(returnCredits);
    }
}
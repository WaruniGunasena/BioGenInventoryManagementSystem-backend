package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.*;
import com.biogenholdings.InventoryMgtSystem.exceptions.NotFoundException;
import com.biogenholdings.InventoryMgtSystem.models.CommissionPayment;
import com.biogenholdings.InventoryMgtSystem.models.MonthlyCommissionInvoice;
import com.biogenholdings.InventoryMgtSystem.models.ProductReturn;
import com.biogenholdings.InventoryMgtSystem.models.SalesCommissionSummary;
import com.biogenholdings.InventoryMgtSystem.repositories.CommissionPaymentRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.MonthlyCommissionInvoiceRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.ProductReturnRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.SalesCommissionSummaryRepository;
import com.biogenholdings.InventoryMgtSystem.services.CommissionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j

public class CommissionServiceImpl implements CommissionService {

    private final SalesCommissionSummaryRepository salesCommissionSummaryRepository;
    private final MonthlyCommissionInvoiceRepository monthlyCommissionInvoiceRepository;
    private final CommissionPaymentRepository commissionPaymentRepository;
    private final ProductReturnRepository productReturnRepository;

    @Override
    public Response getMyCommissions(Long userId, int page, int size) {

        LocalDateTime now = LocalDateTime.now();

        String currentMonthYear = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        LocalDateTime startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime endOfMonth = now.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);

        Pageable pageable = PageRequest.of(page, size);

        Page<SalesCommissionSummary> commissionPage = salesCommissionSummaryRepository
                .findBySalesRepIdAndDateRange(userId, startOfMonth, endOfMonth, pageable);

        BigDecimal monthlyGrandTotal = salesCommissionSummaryRepository
                .sumTotalCommissionBySalesRepIdAndDateRange(userId, startOfMonth, endOfMonth);
        if (monthlyGrandTotal == null) monthlyGrandTotal = BigDecimal.ZERO;

        BigDecimal totalReversal = productReturnRepository.getTotalReversalForRep(userId, currentMonthYear);
        if (totalReversal == null) totalReversal = BigDecimal.ZERO;

        BigDecimal netPayout = monthlyGrandTotal.subtract(totalReversal);

        List<SalesRepCommissionDTO> dtoList = commissionPage.getContent().stream()
                .map(c -> SalesRepCommissionDTO.builder()
                        .invoiceNumber(c.getInvoiceNumber())
                        .customer(c.getCustomer() != null ? c.getCustomer().getName() : "Unknown")
                        .date(c.getInvoiceDate().toLocalDate())
                        .commissionableAmount(c.getCommissionableAmount())
                        .totalCommission(c.getTotalCommission())
                        .build())
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("Current month commissions retrieved successfully")
                .data(dtoList)
                .totalAmountCommissionSalesRep(monthlyGrandTotal)
                .totalAmountCommissionReversal(totalReversal)
                .netPayout(netPayout)
                .currentPage(commissionPage.getNumber())
                .totalItems(commissionPage.getTotalElements())
                .totalPages(commissionPage.getTotalPages())
                .build();
    }


    @Override
    public Response getAllMonthlyInvoices(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<MonthlyCommissionInvoice> invoicePage = monthlyCommissionInvoiceRepository.findAllInvoicesPaginated(pageable);

        List<AdminCommissionDTO> dtoList = invoicePage
                .getContent()
                .stream()
                .map(this::mapToAdminCommissionDTO)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("Monthly commission invoices retrieved successfully")
                .data(dtoList)
                .currentPage(invoicePage.getNumber())
                .totalItems(invoicePage.getTotalElements())
                .totalPages(invoicePage.getTotalPages())
                .build();
    }

    private AdminCommissionDTO mapToAdminCommissionDTO(MonthlyCommissionInvoice invoice) {

        CommissionPayment latestPayment = commissionPaymentRepository
                .findTopByInvoiceIdOrderByIdDesc(invoice.getId());

        BigDecimal dueBalance;
        BigDecimal totalPaid;

        if (latestPayment != null) {
            dueBalance = latestPayment.getDueBalance();
            totalPaid = invoice.getNetPayout().subtract(dueBalance);
        } else {
            dueBalance = invoice.getNetPayout();
            totalPaid = BigDecimal.ZERO;
        }

        return AdminCommissionDTO.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getCommissionInvoiceNumber())
                .monthYear(invoice.getMonthYear())
                .salesRep(invoice.getSalesRep() != null ? invoice.getSalesRep().getName() : "Unknown")
                .monthlyCommissionAmount(invoice.getMonthlyCommission())
                .totalReversalDeduction(invoice.getTotalReversalDeduction())
                .netPayout(invoice.getNetPayout())
                .paymentStatus(invoice.getPayoutStatus())
                .dueBalance(dueBalance)
                .totalPaid(totalPaid)
                .build();
    }

    @Override
    @Transactional
    public Response submitCommissionPayment(CommissionPaymentDTO dto) {
        try {

            MonthlyCommissionInvoice invoice = monthlyCommissionInvoiceRepository.findById(dto.getInvoiceId())
                    .orElseThrow(() -> new NotFoundException("Commission Invoice not found with ID: " + dto.getInvoiceId()));

            if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Payment amount must be greater than zero");
            }

            String paymentMethod = dto.getPaymentMethod() != null ? dto.getPaymentMethod().trim() : "";
            if (paymentMethod.isBlank()) {
                throw new RuntimeException("Payment method is required");
            }

            BigDecimal existingTotalPaid = commissionPaymentRepository.findByInvoiceId(dto.getInvoiceId())
                    .stream()
                    .map(CommissionPayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal newTotalPaid = existingTotalPaid.add(dto.getAmount());

            BigDecimal grandTotal = invoice.getNetPayout();
            BigDecimal dueBalance = grandTotal.subtract(newTotalPaid);

            if (dueBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Payment exceeds the remaining due amount");
            }

            CommissionPayment payment = CommissionPayment.builder()
                    .amount(dto.getAmount())
                    .paymentMethod(paymentMethod)
                    .grandTotal(grandTotal)
                    .dueBalance(dueBalance)
                    .invoice(invoice)
                    .createdBy(dto.getUserId())
                    .createdAt(LocalDateTime.now())
                    .isDeleted(false)
                    .build();

            if (!"cash".equalsIgnoreCase(paymentMethod)) {
                payment.setBank(dto.getBank() != null ? dto.getBank().trim() : null);
                payment.setChequeNumber(dto.getChequeNumber() != null ? dto.getChequeNumber().trim() : null);

                if (dto.getChequeIssueDate() != null && !dto.getChequeIssueDate().isBlank()) {
                    payment.setChequeIssueDate(LocalDate.parse(dto.getChequeIssueDate().trim()));
                }
                if (dto.getChequeDueDate() != null && !dto.getChequeDueDate().isBlank()) {
                    payment.setChequeDueDate(LocalDate.parse(dto.getChequeDueDate().trim()));
                }
            }

            if (dueBalance.compareTo(BigDecimal.ZERO) == 0) {
                invoice.setPayoutStatus("PAID");
            } else if (newTotalPaid.compareTo(BigDecimal.ZERO) > 0) {
                invoice.setPayoutStatus("PARTIAL");
            } else {
                invoice.setPayoutStatus("UNPAID");
            }

            commissionPaymentRepository.save(payment);
            monthlyCommissionInvoiceRepository.save(invoice);

            return Response.builder()
                    .status(200)
                    .message("Commission payment of Rs. " + dto.getAmount() + " recorded successfully")
                    .build();

        } catch (Exception e) {
            return Response.builder()
                    .status(500)
                    .message("Failed to process commission payment: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Response getCommissionInvoiceDetails(String commissionInvoiceNumber) {

        MonthlyCommissionInvoice monthlyInvoice = monthlyCommissionInvoiceRepository
                .findByCommissionInvoiceNumber(commissionInvoiceNumber)
                .orElseThrow(() -> new NotFoundException("Monthly statement not found: " + commissionInvoiceNumber));

        Long repId = monthlyInvoice.getSalesRep().getId();
        String monthYear = monthlyInvoice.getMonthYear();

        List<SalesCommissionSummary> commissionDetails = salesCommissionSummaryRepository
                .findByRepAndMonth(repId, monthYear);

        List<SalesRepCommissionDTO> commissionDtoList = commissionDetails.stream()
                .map(record -> SalesRepCommissionDTO.builder()
                        .invoiceNumber(record.getInvoiceNumber())
                        .customer(record.getCustomer() != null ? record.getCustomer().getName() : "Unknown")
                        .date(record.getInvoiceDate().toLocalDate())
                        .commissionableAmount(record.getCommissionableAmount())
                        .totalCommission(record.getTotalCommission())
                        .build())
                .collect(Collectors.toList());

        List<ProductReturn> reversalDetails = productReturnRepository
                .findByRepAndMonth(repId, monthYear);

        List<CommissionReversalDTO> reversalDtoList = reversalDetails.stream()
                .map(pr -> CommissionReversalDTO.builder()
                        .invoiceNumber(pr.getSalesOrder() != null ? pr.getSalesOrder().getInvoiceNumber() : "N/A")
                        .customerName(pr.getCustomer() != null ? pr.getCustomer().getName() : "Unknown")
                        .salesRepName(pr.getSalesRep() != null ? pr.getSalesRep().getName() : "N/A")
                        .invoiceDate(pr.getReturnDate())
                        .totalReturnAmount(pr.getTotalReturnAmount())
                        .totalCommissionReversal(pr.getTotalCommissionReversal())
                        .build())
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("Breakdown for " + monthYear + " retrieved successfully")
                .data(commissionDtoList)
                .reversalData(reversalDtoList)
                .build();
    }

    @Override
    public Response getMyCommissionHistory(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<MonthlyCommissionInvoice> invoicePage = monthlyCommissionInvoiceRepository.findBySalesRepId(userId, pageable);

        List<AdminCommissionDTO> dtoList = invoicePage.getContent().stream()
                .map(this::mapToAdminCommissionDTO)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("Personal commission history retrieved successfully")
                .data(dtoList)
                .currentPage(invoicePage.getNumber())
                .totalItems(invoicePage.getTotalElements())
                .totalPages(invoicePage.getTotalPages())
                .build();
    }

    @Override
    public Response getMyCommissionReversals(Long userId, int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime endOfMonth = now.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);

        Pageable pageable = PageRequest.of(page, size);

        Page<ProductReturn> reversalPage = productReturnRepository
                .findReversalsBySalesRepAndDateRange(userId, startOfMonth, endOfMonth, pageable);

        List<CommissionReversalDTO> dtoList = reversalPage.getContent().stream()
                .map(pr -> CommissionReversalDTO.builder()
                        .invoiceNumber(pr.getSalesOrder() != null ? pr.getSalesOrder().getInvoiceNumber() : "N/A")
                        .customerName(pr.getCustomer() != null ? pr.getCustomer().getName() : "Unknown")
                        .salesRepName(pr.getSalesRep() != null ? pr.getSalesRep().getName() : "N/A")
                        .invoiceDate(pr.getReturnDate())
                        .totalReturnAmount(pr.getTotalReturnAmount())
                        .totalCommissionReversal(pr.getTotalCommissionReversal())
                        .build())
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("Monthly commission reversals retrieved successfully")
                .data(dtoList)
                .currentPage(reversalPage.getNumber())
                .totalItems(reversalPage.getTotalElements())
                .totalPages(reversalPage.getTotalPages())
                .build();
    }
}

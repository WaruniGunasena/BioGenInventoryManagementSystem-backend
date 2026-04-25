package com.biogenholdings.InventoryMgtSystem.tasks;

import com.biogenholdings.InventoryMgtSystem.enums.UserRole;
import com.biogenholdings.InventoryMgtSystem.models.User;
import com.biogenholdings.InventoryMgtSystem.models.MonthlyCommissionInvoice;
import com.biogenholdings.InventoryMgtSystem.repositories.MonthlyCommissionInvoiceRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.SalesCommissionSummaryRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CommissionTask {

    private final SalesCommissionSummaryRepository summaryRepository;
    private final MonthlyCommissionInvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    /**
     * Executes at 00:00:00 (Midnight) on the Last (L) day of every month.
     * Processes every active Sales Rep and generates their consolidated invoice.
     */
    @Scheduled(cron = "0 23 11 25 * ?")
    @Transactional
    public void generateMonthlyInvoices() {
        // Formats current month as "2026-04" to match your DB search patterns
        String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // Fetch only active, non-deleted Sales Representatives
        List<User> salesReps = userRepository.findByRoleAndIsDeletedFalse(UserRole.SALES_REP);

        for (User rep : salesReps) {
            // Calculates the sum of 'total_commission' for the rep in the specified month
            BigDecimal monthlyTotal = summaryRepository.getMonthlyTotalForRep(rep.getId(), monthYear);

            // Only generate an invoice if they actually earned something
            if (monthlyTotal != null && monthlyTotal.compareTo(BigDecimal.ZERO) > 0) {

                MonthlyCommissionInvoice invoice = MonthlyCommissionInvoice.builder()
                        .commissionInvoiceNumber("STMT-" + monthYear + "-REP" + rep.getId())
                        .salesRepId(rep.getId())
                        .monthYear(monthYear)
                        .MonthlyCommission(monthlyTotal) // Matches your summary logic
                        .payoutStatus("UNPAID")
                        .generatedDate(LocalDateTime.now())
                        .build();

                invoiceRepository.save(invoice);
            }
        }
    }
}
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

    @Scheduled(cron = "0 0 0 L * ?")
    @Transactional
    public void generateMonthlyInvoices() {

        String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        List<User> salesReps = userRepository.findByRoleAndIsDeletedFalse(UserRole.SALES_REP);

        for (User rep : salesReps) {

            BigDecimal monthlyTotal = summaryRepository.getMonthlyTotalForRep(rep.getId(), monthYear);

            if (monthlyTotal != null && monthlyTotal.compareTo(BigDecimal.ZERO) > 0) {

                MonthlyCommissionInvoice invoice = MonthlyCommissionInvoice.builder()
                        .commissionInvoiceNumber("BHG-" + monthYear + "-REP" + rep.getId())
                        .salesRep(rep)
                        .monthYear(monthYear)
                        .MonthlyCommission(monthlyTotal)
                        .payoutStatus("UNPAID")
                        .generatedDate(LocalDateTime.now())
                        .build();

                invoiceRepository.save(invoice);
            }
        }
    }
}
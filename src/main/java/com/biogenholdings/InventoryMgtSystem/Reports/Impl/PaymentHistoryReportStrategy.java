package com.biogenholdings.InventoryMgtSystem.Reports.Impl;

import com.biogenholdings.InventoryMgtSystem.Reports.ReportStrategy;
import com.biogenholdings.InventoryMgtSystem.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentHistoryReportStrategy implements ReportStrategy {
    final private ReportRepository reportRepo;
    @Override public String getReportIdentifier() { return "PAYMENT_HISTORY"; }
    @Override public String getReportName() { return "Customer Payment History Log"; }

    @Override
    public List<Map<String, Object>> getReportData(Map<String, String> params) {
        // We use LocalDateTime for detailed history
        LocalDate start = LocalDate.parse(params.getOrDefault("startDate", LocalDate.now().toString()));
        LocalDate end = LocalDate.parse(params.getOrDefault("endDate", LocalDate.now().toString()));

        return reportRepo.getPaymentHistory(start.atStartOfDay(), end.atTime(23, 59, 59));
    }
}

package com.biogenholdings.InventoryMgtSystem.services;

import com.biogenholdings.InventoryMgtSystem.dtos.CommissionPaymentDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;

public interface CommissionService {

    Response getAllMonthlyInvoices(int page, int size);

    Response getMyCommissions(Long userId, int page, int size);

    Response submitCommissionPayment(CommissionPaymentDTO dto);

    Response getCommissionInvoiceDetails(String invoiceNumber);

    Response getMyCommissionHistory(Long userId, int page, int size);

    Response getMyCommissionReversals(Long userId, int page, int size);
}

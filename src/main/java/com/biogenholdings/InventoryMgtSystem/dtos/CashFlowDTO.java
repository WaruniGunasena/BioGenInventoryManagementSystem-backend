package com.biogenholdings.InventoryMgtSystem.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CashFlowDTO {

    private List<DebitDTO> debits;
    private List<CreditDTO> credits;
}

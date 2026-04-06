package com.biogenholdings.InventoryMgtSystem.projections;

import java.math.BigDecimal;

public interface CustomerDueProjection {

    Long getCustomerId();     // match alias
    BigDecimal getTotalDue();
}

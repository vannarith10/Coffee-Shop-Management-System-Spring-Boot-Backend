package com.coffeeshop.api.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneyHelper {
    private MoneyHelper () {}

    // Return v if not null, otherwise 0
    public static BigDecimal nullToZero (BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    public static double safeMoney (BigDecimal val) {
        return val.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static double growthPct (BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }

        return current.subtract(previous)
                .divide(previous, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.valueOf(100))
                .doubleValue();
    }
}

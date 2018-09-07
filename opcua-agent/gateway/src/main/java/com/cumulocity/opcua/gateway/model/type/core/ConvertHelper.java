package com.cumulocity.opcua.gateway.model.type.core;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class ConvertHelper {

    public static Double convertToDouble(double value, Conversion conversion) {
        double convertedValue = multiply(value, conversion.getMultiplier());
        convertedValue = divide(convertedValue, conversion.getDivisor());
        convertedValue = add(convertedValue, conversion.getOffset());
        convertedValue = formatDecimal(convertedValue, conversion.getDecimalPlaces());
        return convertedValue;
    }

    private static double add(double value, Double offset) {
        if (offset == null) {
            return value;
        }
        return value + offset;
    }

    private static double divide(double value, Double divisor) {
        if (divisor == null) {
            return value;
        }
        return value / divisor;
    }

    private static double formatDecimal(double value, int places) {
        BigDecimal multipliedDecimal = new BigDecimal(value * Math.pow(10, places));
        double truncatedDouble = multipliedDecimal.setScale(0, RoundingMode.DOWN).doubleValue();
        return truncatedDouble / Math.pow(10, places);
    }

    private static double multiply(double value, Double multiplier) {
        if (multiplier == null) {
            return value;
        }
        return value * multiplier;
    }
}

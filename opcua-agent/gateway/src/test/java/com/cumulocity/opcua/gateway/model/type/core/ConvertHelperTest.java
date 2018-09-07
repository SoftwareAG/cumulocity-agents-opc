package com.cumulocity.opcua.gateway.model.type.core;

import com.cumulocity.opcua.gateway.model.type.Register;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConvertHelperTest {
    @Test
    public void shouldConsiderDecimalPlacesWhileConvertToDouble() {
        Register register = Register.builder()
                .divisor(1d)
                .multiplier(1d)
                .offset(0d)
                .decimalPlaces(4)
                .build();

        assertThat(ConvertHelper.convertToDouble(123.456789101112d, register)).isEqualTo(123.4567d);
        assertThat(ConvertHelper.convertToDouble(123.0d, register)).isEqualTo(123.0d);

        register = Register.builder()
                .divisor(1d)
                .multiplier(1d)
                .offset(0.0000111d)
                .decimalPlaces(5)
                .build();

        assertThat(ConvertHelper.convertToDouble(123.0d, register)).isEqualTo(123.00001d);
    }
}

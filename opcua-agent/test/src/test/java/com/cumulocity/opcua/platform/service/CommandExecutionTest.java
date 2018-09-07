package com.cumulocity.opcua.platform.service;

import c8y.TemperatureMeasurement;
import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.configuration.BaseIntegrationTest;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.mock.configuration.EventWatcher;
import com.cumulocity.opcua.mock.platform.service.MeasurementMockService.MeasurementAdded;
import com.cumulocity.opcua.platform.model.Command;
import com.cumulocity.rest.representation.measurement.MeasurementRepresentation;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandExecutionTest extends BaseIntegrationTest {

    @Autowired
    private CommandService commandService;

    @Autowired
    private EventWatcher eventWatcher;

    @After
    public void tearDown() throws Exception {
        commandService.destroy();
    }

    @Test
    public void shouldExecuteCommandSynchronouslyIfDelayIsZero() {
        //given
        final BigDecimal temperature = BigDecimal.valueOf(36.6d);

        final TemperatureMeasurement temperatureMeasurement = new TemperatureMeasurement();
        temperatureMeasurement.setTemperature(temperature);

        final MeasurementRepresentation representation = new MeasurementRepresentation();
        representation.set(temperatureMeasurement);

        final Gateway gateway = Gateway.gateway().tenant("management").name("user").password("pass").id(GId.asGId("1234")).build();

        //when
        commandService.queueForExecution(gateway, new Command(representation));

        //then
        final MeasurementAdded measurement = eventWatcher.waitFor(MeasurementAdded.class);
        assertThat(measurement.getMeasurement().get(TemperatureMeasurement.class).getTemperature()).isEqualTo(temperature);
    }
 
    @Test
    public void shouldStoreCommandsPerGateway() {
        //given
        final Gateway gateway1 = Gateway.gateway().tenant("management").name("user").password("pass").id(GId.asGId("1234")).build();
        final Gateway gateway2 = Gateway.gateway().tenant("test").name("user").password("pass").id(GId.asGId("5678")).pollingRateInSeconds(100D).transmitRateInSeconds(100L).build();
        final Command command1 = new Command(new MeasurementRepresentation());
        final Command command2 = new Command(new MeasurementRepresentation());

        //when
        commandService.queueForExecution(gateway1, command1);
        commandService.queueForExecution(gateway2, command2);

        //then
        assertThat(commandService.getCommands()).hasSize(1);
    }

}

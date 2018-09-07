package com.cumulocity.opcua.gateway.factory;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.model.type.*;
import com.cumulocity.opcua.gateway.model.type.core.BrowsePath;
import com.cumulocity.opcua.gateway.model.type.core.BrowsePathElement;
import com.cumulocity.opcua.gateway.model.type.mapping.AlarmMapping;
import com.cumulocity.opcua.gateway.model.type.mapping.EventMapping;
import com.cumulocity.opcua.gateway.model.type.mapping.MeasurementMapping;
import com.cumulocity.opcua.gateway.model.type.mapping.StatusMapping;
import com.cumulocity.opcua.gateway.repository.configuration.RepositoryConfiguration;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.google.common.base.Optional;
import org.junit.Test;
import org.svenson.JSONParser;

import java.io.IOException;

import static com.cumulocity.opcua.Conditions.present;
import static com.cumulocity.opcua.gateway.model.type.mapping.AlarmSeverity.WARNING;
import static com.cumulocity.opcua.gateway.model.type.Status.write;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.not;

public class DeviceTypeFactoryTest {

    private final DeviceTypeFactory deviceTypeFactory = new DeviceTypeFactory(new RepositoryConfiguration().objectMapper());
    private final JSONParser jsonParser = new JSONParser();

    @Test
    public void shouldNotCreate() {
        final Optional<DeviceType> deviceType = deviceTypeFactory.create(new ManagedObjectRepresentation());

        assertThat(deviceType).is(not(present()));
    }

    @Test
    public void shouldCreate() throws IOException {
        final ManagedObjectRepresentation managedObject = createManagedObject();

        final Optional<DeviceType> deviceType = deviceTypeFactory.create(managedObject);

        final DeviceType expected = DeviceType.builder()
                .id(GId.asGId("123"))
                .name("ModbusTemperature")
                .type("c8y_ModbusDeviceType")
                .fieldbusType("opcua")
                .useServerTime(true)
                .register(Register.builder()
                        .name("Temperature")
                        .divisor(1d)
                        .decimalPlaces(5)
                        .multiplier(1d)
                        .unit("U")
                        .min(1d)
                        .max(256d)
                        .browsePath(new BrowsePath(newArrayList(new BrowsePathElement(0, "browse"), new BrowsePathElement(0, "path"))))
                        .category("category")
                        .measurementMapping(MeasurementMapping.builder()
                                .type("c8y_TemperatureMeasurement")
                                .series("S")
                                .build())
                        .alarmMapping(AlarmMapping.alarmMapping()
                                .type("c8y_TemperatureAlarm")
                                .text("Temperature alarm")
                                .severity(WARNING)
                                .build())
                        .eventMapping(EventMapping.builder()
                                .type("c8y_TemperatureEvent")
                                .text("Temperature event")
                                .build())
                        .statusMapping(StatusMapping.builder()
                                .type(write)
                                .build())
                        .build())
                .build();
        assertThat(deviceType).is(present());
        assertThat(deviceType.get()).isEqualTo(expected);
    }

    private ManagedObjectRepresentation createManagedObject() {
        final ManagedObjectRepresentation managedObject = jsonParser.parse(ManagedObjectRepresentation.class, deviceTypeJsonWithoutRestTemplates());
        managedObject.setId(GId.asGId("123"));
        return managedObject;
    }

    private String deviceTypeJsonWithoutRestTemplates() {
        return "{\n" +
                "   \"name\":\"ModbusTemperature\",\n" +
                "   \"fieldbusType\":\"opcua\",\n" +
                "   \"type\":\"c8y_ModbusDeviceType\",\n" +
                "   \"c8y_Coils\":[\n" +
                "   ],\n" +
                "   \"c8y_Registers\":[\n" +
                "      {\n" +
                "         \"name\":\"Temperature\",\n" +
                "         \"browsePath\":\"/browse/path\",\n" +
                "         \"fieldbusType\":null,\n" +
                "         \"number\":1,\n" +
                "         \"multiplier\":1,\n" +
                "         \"divisor\":1,\n" +
                "         \"decimalPlaces\":5,\n" +
                "         \"startBit\":0,\n" +
                "         \"noBits\":16,\n" +
                "         \"unit\":\"U\",\n" +
                "         \"signed\":false,\n" +
                "         \"input\":false,\n" +
                "         \"min\":1,\n" +
                "         \"max\":256,\n" +
                "         \"category\":\"category\",\n" +
                "         \"measurementMapping\":{\n" +
                "            \"type\":\"c8y_TemperatureMeasurement\",\n" +
                "            \"series\":\"S\",\n" +
                "            \"sendMeasurementTemplate\":301\n" +
                "         },\n" +
                "         \"alarmMapping\":{\n" +
                "            \"type\":\"c8y_TemperatureAlarm\",\n" +
                "            \"text\":\"Temperature alarm\",\n" +
                "            \"severity\":\"WARNING\",\n" +
                "            \"raiseAlarmTemplate\":300\n" +
                "         },\n" +
                "         \"eventMapping\":{\n" +
                "            \"type\":\"c8y_TemperatureEvent\",\n" +
                "            \"text\":\"Temperature event\",\n" +
                "            \"eventTemplate\":302\n" +
                "         },\n" +
                "         \"statusMapping\":{\n" +
                "            \"status\":\"write\"\n" +
                "         },\n" +
                "         \"id\":\"8305450801818488\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"c8y_useServerTime\":true,\n" +
                "   \"com_cumulocity_model_smartrest_SmartRestTemplate\":{\n" +
                "   }\n" +
                "}";
    }
}

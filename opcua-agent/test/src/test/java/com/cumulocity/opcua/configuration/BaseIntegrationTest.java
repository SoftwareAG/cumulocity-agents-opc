package com.cumulocity.opcua.configuration;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.GatewayMain;
import com.cumulocity.opcua.gateway.model.type.DeviceType;
import com.cumulocity.opcua.gateway.model.type.core.BrowsePath;
import com.cumulocity.opcua.gateway.repository.core.GatewayRepository;
import com.cumulocity.opcua.gateway.service.BootstrapService;
import com.cumulocity.opcua.gateway.service.configuration.GatewayConfigurationProperties;
import com.cumulocity.opcua.mock.configuration.EventWatcher;
import com.cumulocity.opcua.mock.opcua.ServerBuilder;
import com.cumulocity.opcua.persistance.repository.DBStore;
import com.cumulocity.opcua.mock.notification.configuration.NotificationConfig;
import com.cumulocity.opcua.mock.platform.service.DeviceControlService;
import com.cumulocity.opcua.mock.platform.service.InventoryMockService;
import com.cumulocity.opcua.mock.platform.service.MeasurementMockService;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.cumulocity.opcua.gateway.model.common.SimpleTypeUtils.GID_PREFIX;
import static com.cumulocity.opcua.gateway.model.device.Device.c8y_OPCUADevice;

//@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
@IntegrationTest
@WebAppConfiguration
@SpringApplicationConfiguration(classes = {
        GatewayMain.class,
        TestConfiguration.class,
        NotificationConfig.class
})
public abstract class BaseIntegrationTest {

//    todo wojtek refactor me please
    @Autowired
    protected InventoryMockService inventoryMockService;

    @Autowired
    protected MeasurementMockService measurementMockService;

    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    @Autowired
    protected EventWatcher eventWatcher;

    @Autowired
    protected BootstrapService bootstrapService;

    @Autowired
    protected DeviceControlService deviceControlService;

    @Autowired
    protected GatewayRepository<DeviceType> deviceTypeRepository;

    @Autowired
    protected DBStore db;

    @Autowired
    protected ServerBuilder server;

    @Autowired
    protected GatewayConfigurationProperties properties;

    @Before
    public void setUp() {
        inventoryMockService.clear();
        measurementMockService.clear();
        eventWatcher.clear();
        db.clearAll();
    }

    protected ManagedObjectRepresentation givenDeviceType(BrowsePath browsePath) {
        final ManagedObjectRepresentation register = new ManagedObjectRepresentation();
        register.setProperty("browsePath", browsePath.asString());
        final List<ManagedObjectRepresentation> registerList = new ArrayList<>();
        registerList.add(register);

        final ManagedObjectRepresentation managedObject = new ManagedObjectRepresentation();
        managedObject.setProperty("fieldbusType", "opcua");
        managedObject.setProperty("c8y_Registers", registerList);
        return inventoryMockService.store(managedObject);
    }

    protected ManagedObjectRepresentation givenDevice(BrowsePath browsePath, GId deviceType) {
        final HashMap<Object, Object> fragment = new HashMap<>();
        fragment.put("browsePath", browsePath.asString());
        if (deviceType != null) {
            fragment.put("type", GID_PREFIX + deviceType.getValue());
        }

        final ManagedObjectRepresentation child = new ManagedObjectRepresentation();
        child.set(fragment, c8y_OPCUADevice);
        return inventoryMockService.store(child);
    }
}

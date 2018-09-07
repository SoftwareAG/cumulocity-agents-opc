package com.cumulocity.opcua.gateway.factory;

import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.type.core.BrowsePath;
import com.cumulocity.opcua.gateway.model.type.core.BrowsePathElement;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.google.common.base.Optional;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static com.cumulocity.model.idtype.GId.asGId;
import static com.cumulocity.opcua.Conditions.equalTo;
import static com.cumulocity.opcua.Conditions.present;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

public class DeviceFactoryTest {

    private DeviceFactory deviceFactory = new DeviceFactory();

    @Test
    public void shouldCreate() {
        final HashMap<Object, Object> fragment = new HashMap<>();
        fragment.put("browsePath", "/myk/myk/myk/disco/polo");
        fragment.put("type",  "/inventory/managedObjects/10601");
        final ManagedObjectRepresentation managedObject = new ManagedObjectRepresentation();
        managedObject.setId(asGId("10600"));
        managedObject.setProperty("c8y_OPCUADevice", fragment);

        final Optional<Device> deviceOptional = deviceFactory.create(managedObject);

        final BrowsePath expectedBrowsePath = new BrowsePath(newArrayList(
                new BrowsePathElement(0, "myk"),
                new BrowsePathElement(0, "myk"),
                new BrowsePathElement(0, "myk"),
                new BrowsePathElement(0, "disco"),
                new BrowsePathElement(0, "polo"))
        );
        assertThat(deviceOptional).is(present());
        assertThat(deviceOptional.get()).isEqualTo(new Device(asGId("10600"), expectedBrowsePath, asGId("10601")));
    }

    @Test
    public void shouldNotCreate() {
        final ManagedObjectRepresentation representation = new ManagedObjectRepresentation();

        final Optional<Device> deviceOptional = deviceFactory.create(representation);

        assertThat(deviceOptional).isNot(present());
    }
}

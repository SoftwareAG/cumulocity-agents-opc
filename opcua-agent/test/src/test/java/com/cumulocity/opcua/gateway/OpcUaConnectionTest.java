package com.cumulocity.opcua.gateway;

import com.cumulocity.opcua.client.model.Client;
import com.cumulocity.opcua.client.service.configuration.ClientFactory;
import com.cumulocity.opcua.configuration.BaseIntegrationTest;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.mock.opcua.ServerBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.cumulocity.opcua.client.OpcUaUtils.component;
import static com.cumulocity.opcua.client.OpcUaUtils.referenceTo;
import static com.cumulocity.opcua.gateway.model.type.core.BrowsePath.asBrowsePath;
import static com.cumulocity.opcua.gateway.repository.configuration.ContextProvider.doInvoke;
import static org.assertj.core.api.Assertions.assertThat;

public class OpcUaConnectionTest extends BaseIntegrationTest {

    @Autowired
    private ServerBuilder serverBuilder;

    @Autowired
    private ClientFactory clientProvider;

    @Test
//    @Ignore
    public void shouldConnectWithServer() {
        //given
        final String nodeName = "TestDevice";

        //when
        serverBuilder.createObjectNode(asBrowsePath(nodeName)).start();

        //then
        doInvoke(Gateway.gateway().url("opc.tcp://localhost:52520/OPCUA/IntegrationTestServer").applicationUri("urn:localhost:OPCUA:TestClient").build(), new Runnable() {
            @Override
            public void run() {
                final Client client = clientProvider.client();
                assertThat(client.isConnected()).isTrue();
                assertThat(client).has(referenceTo(nodeName));
                assertThat(client).has(component(nodeName));
            }
        });
    }
}

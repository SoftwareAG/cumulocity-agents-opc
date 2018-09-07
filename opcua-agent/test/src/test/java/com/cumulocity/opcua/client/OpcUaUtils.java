package com.cumulocity.opcua.client;

import com.cumulocity.opcua.client.model.Client;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaReference;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;

import static org.opcfoundation.ua.core.Identifiers.ObjectsFolder;

@UtilityClass
public class OpcUaUtils {
    public static Condition<Client> referenceTo(final String nameName) {
        return new Condition<Client>() {
            @Override
            @SneakyThrows
            public boolean matches(Client client) {
                final UaNode node = client.getAddressSpace().getNode(ObjectsFolder);
                for (UaReference reference : node.getReferences()) {
                    if (StringUtils.equals(reference.getTargetNode().getDisplayName().getText(), nameName)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static Condition<Client> component(final String displayName) {
        return new Condition<Client>() {
            @Override
            @SneakyThrows
            public boolean matches(Client client) {
                final UaNode node = client.getAddressSpace().getNode(ObjectsFolder);
                for (UaNode component : node.getComponents()) {
                    if (StringUtils.equals(component.getDisplayName().getText(), displayName)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}

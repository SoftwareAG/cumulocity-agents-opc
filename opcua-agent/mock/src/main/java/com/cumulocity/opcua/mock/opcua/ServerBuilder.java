package com.cumulocity.opcua.mock.opcua;

import com.cumulocity.opcua.client.model.ExpandedNodeId;
import com.cumulocity.opcua.gateway.model.type.core.BrowsePath;
import com.cumulocity.opcua.gateway.model.type.core.BrowsePathElement;
import com.cumulocity.opcua.gateway.service.utils.Consumer;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaInstance;
import com.prosysopc.ua.nodes.UaObjectType;
import com.prosysopc.ua.nodes.UaType;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.nodes.CacheVariable;
import com.prosysopc.ua.server.nodes.UaInstanceNode;
import com.prosysopc.ua.server.nodes.UaObjectNode;
import com.prosysopc.ua.server.nodes.UaObjectTypeNode;
import com.prosysopc.ua.types.opcua.FolderType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.String;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Locale.ENGLISH;
import static org.opcfoundation.ua.core.Identifiers.*;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ServerBuilder {

    private final UaServer server;

    private NodeManagerUaNode nodeManager;
    private int namespaceIndex;
    private FolderType objectsFolder;
    private FolderType rootFolder;
    private UaType baseObjectType;
    private UaType baseDataVariableType;

    @Getter(lazy = true)
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(256);

    @PostConstruct
    public void init() throws Exception {
        this.nodeManager = new NodeManagerUaNode(server, "http://www.cumulocity.com/OPCUA/IntegrationTestAddressSpace");
        this.namespaceIndex = nodeManager.getNamespaceIndex();
        this.objectsFolder = server.getNodeManagerRoot().getObjectsFolder();
        this.rootFolder = server.getNodeManagerRoot().getRoot();
        this.baseObjectType = server.getNodeManagerRoot().getType(BaseObjectType);
        this.baseDataVariableType = server.getNodeManagerRoot().getType(BaseDataVariableType);
    }

    @SneakyThrows
    public ServerBuilder createObjectNode(final BrowsePath browsePath) {
        return createObjectNode(browsePath, null);
    }

    @SneakyThrows
    public ServerBuilder createObjectNode(final BrowsePath browsePath, final Consumer<UaInstance> consumer) {
        UaInstance parent = objectsFolder;
        for (final Iterator<BrowsePathElement> iterator = browsePath.withNamespaceIndex(namespaceIndex).iterator(); iterator.hasNext(); ) {
            final BrowsePathElement element = iterator.next();
            final String name = element.getName();
            final boolean last = !iterator.hasNext();
            final UaObjectType type = createType(name + "Type", last);
            parent = createNode(name, type, parent, last);
        }
        if (consumer !=  null) {
            final UaInstance finalParent = parent;
            getScheduler().scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        consumer.apply(finalParent);
                    } catch (final Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }
            }, 1, 1, TimeUnit.SECONDS);
        }

        return this;
    }

    private UaInstanceNode createNode(String name, UaObjectType typeNode, UaInstance parent, boolean isValue) throws StatusException {
        final org.opcfoundation.ua.builtintypes.NodeId nodeId = new NodeId(namespaceIndex, name);
        if (nodeManager.hasNode(nodeId)) {
            return (UaInstanceNode) nodeManager.getNode(nodeId);
        } else {
            if (isValue) {
                final CacheVariable myDevice = new CacheVariable(nodeManager, nodeId, name, ENGLISH);
                myDevice.setTypeDefinition(typeNode);
                nodeManager.addNodeAndReference(parent, myDevice, HasComponent);
                return myDevice;
            } else {
                final UaObjectNode myDevice = new UaObjectNode(nodeManager, nodeId, name, ENGLISH);
                myDevice.setTypeDefinition(typeNode);
                nodeManager.addNodeAndReference(parent, myDevice, HasComponent);
                return myDevice;
            }
        }
    }

    private UaObjectType createType(String name, boolean isValue) throws StatusException {
        final org.opcfoundation.ua.builtintypes.NodeId nodeId = new NodeId(namespaceIndex, name);
        if (nodeManager.hasNode(nodeId)) {
            return (UaObjectType) nodeManager.getNode(nodeId);
        } else {
            final UaObjectType typeNode = new UaObjectTypeNode(nodeManager, nodeId, name, ENGLISH);
            if (isValue) {
                nodeManager.addNodeAndReference(baseDataVariableType, typeNode, HasSubtype);
            } else {
                nodeManager.addNodeAndReference(baseObjectType, typeNode, HasSubtype);
            }
            return typeNode;
        }
    }

    @SneakyThrows
    public void start() {
        if (!server.isRunning()) {
            server.start();
        }
    }

    public void close() {
        server.close();
    }
}

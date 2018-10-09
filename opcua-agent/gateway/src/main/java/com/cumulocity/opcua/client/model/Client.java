package com.cumulocity.opcua.client.model;

import com.cumulocity.opcua.gateway.model.operation.Operation;
import com.cumulocity.opcua.gateway.model.type.core.BrowsePath;
import com.cumulocity.opcua.gateway.model.type.core.BrowsePathElement;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.prosysopc.ua.client.AddressSpace;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaProperty;
import com.prosysopc.ua.nodes.UaType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.core.*;

import java.util.List;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.FluentIterable.from;
import static lombok.AccessLevel.PACKAGE;
import static org.opcfoundation.ua.core.Identifiers.HierarchicalReferences;
import static org.opcfoundation.ua.core.StatusCodes.Bad_NoMatch;

@RequiredArgsConstructor
public class Client {
    @Getter(PACKAGE)
    private final UaClient client;

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    public void disconnect() {
        if (isConnected()) {
            client.disconnect();
        }
    }

    @SneakyThrows
    public void addSubscription(Subscription subscription) {
        client.addSubscription(subscription.getTarget());
    }

    @SneakyThrows
    public void removeSubscription(Subscription subscription) {
        client.removeSubscription(subscription.getTarget());
    }

//    todo hide library internal api
    public AddressSpace getAddressSpace() {
        return client.getAddressSpace();
    }

    @SneakyThrows
    public boolean writeAttribute(Operation operation) {
        final Optional<ExpandedNodeId> expandedNode = targetNodeId(operation.getBrowsePath());

        if (expandedNode.isPresent()) {
            final UaNode node = getAddressSpace().getNode(expandedNode.get().getTarget());

            if (node instanceof UaProperty) {
                final UaProperty property = (UaProperty) node;
                final UaType dataType = property.getDataType();
                final Class<?> javaClass = dataType.getJavaClass();
                if (Double.class.equals(javaClass)) {
                    client.writeAttribute(node.getNodeId(), Attributes.Value, operation.doubleValue());
                } else if (Integer.class.equals(javaClass)) {
                    client.writeAttribute(node.getNodeId(), Attributes.Value, operation.intValue());
                } else if (Long.class.equals(javaClass)) {
                    client.writeAttribute(node.getNodeId(), Attributes.Value, operation.longValue());
                } else if (String.class.equals(javaClass)) {
                    client.writeAttribute(node.getNodeId(), Attributes.Value, operation.stringValue());
                }
            }
            return true;
        }
        return false;
    }

    @SneakyThrows
    public Optional<ExpandedNodeId> targetNodeId(final BrowsePath fullBrowsePath) {
        final List<RelativePathElement> elements = absoluteBrowsePath(fullBrowsePath);
        final NodeId rootFolder = findRootFolder(elements);

        final Client client = this;
        final AddressSpace addressSpace = client.getAddressSpace();
//        addressSpace.setReferenceTypeId(Identifiers.HierarchicalReferences);
        final BrowsePathResult[] browsePathResults = addressSpace.translateBrowsePathsToNodeIds(rootFolder, new RelativePath(elements.toArray(new RelativePathElement[elements.size()])));
        final BrowsePathResult browsePathResult = browsePathResults[0];
        if (Bad_NoMatch.equals(browsePathResult.getStatusCode().getValue())) {
            return absent();
        }
        return fromNullable(new ExpandedNodeId(browsePathResult.getTargets()[0].getTargetId()));
    }

    private NodeId findRootFolder(List<RelativePathElement> elements) {
        NodeId rootFolder = Identifiers.RootFolder;
        if (elements.size() > 0) {
            final RelativePathElement root = elements.get(0);
            final QualifiedName target = root.getTargetName();
            if (target.getNamespaceIndex() != 0) {
                rootFolder = Identifiers.ObjectsFolder;
            }
        }
        return rootFolder;
    }

    private List<RelativePathElement> absoluteBrowsePath(final BrowsePath fullBrowsePath) {
        return from(fullBrowsePath).transform(new Function<BrowsePathElement, RelativePathElement>() {
            public RelativePathElement apply(BrowsePathElement element) {
                return new RelativePathElement(HierarchicalReferences, false, true, qualifiedName(element));
            }
        }).filter(new Predicate<RelativePathElement>() {
            public boolean apply(RelativePathElement element) {
                final QualifiedName target = element.getTargetName();
                if ("Root".equalsIgnoreCase(target.getName()) && target.getNamespaceIndex() == 0) {
                    return false;
                }
                return true;
            }
        }).toList();
    }

    private QualifiedName qualifiedName(BrowsePathElement element) {
        return new QualifiedName(namespaceIndex(element), element.getName());
    }

    private int namespaceIndex(BrowsePathElement element) {
        return element.namespaceName().transform(new Function<String, Integer>() {
            public Integer apply(String name) {
                return Client.this.getAddressSpace().getNamespaceTable().getIndex(name);
            }
        }).or(element.getNamespaceIndex());
    }
}

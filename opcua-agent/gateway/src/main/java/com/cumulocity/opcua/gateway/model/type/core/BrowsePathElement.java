package com.cumulocity.opcua.gateway.model.type.core;

import com.google.common.base.Optional;
import lombok.Data;
import lombok.Value;
import lombok.experimental.Wither;
import org.apache.commons.lang3.StringUtils;


@Data
public class BrowsePathElement {
    @Wither
    private final int namespaceIndex;

    @Wither
    private final String namespaceName;

    @Wither
    private final String name;

    public BrowsePathElement() {
        this(null);
    }

    public BrowsePathElement(String name) {
        this(0, null, name);
    }

    public BrowsePathElement(int namespaceId, String name) {
        this(namespaceId, null, name);
    }

    public BrowsePathElement(String namespaceName, String name) {
        this(0, namespaceName, name);
    }

    public BrowsePathElement(int namespaceIndex, String namespaceName, String name) {
        this.namespaceIndex = namespaceIndex;
        this.namespaceName = namespaceName;
        this.name = name;
    }

    public Optional<String> namespaceName() {
        return Optional.fromNullable(namespaceName);
    }
}

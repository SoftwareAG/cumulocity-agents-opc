package com.cumulocity.opcua.gateway.model.type.core;

import com.cumulocity.opcua.gateway.model.core.HasBrowsePath;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import lombok.Data;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;

import static com.cumulocity.opcua.gateway.model.common.SimpleTypeUtils.parseInt;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.transform;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.util.StringUtils.isEmpty;

@Data
public class BrowsePath implements Iterable<BrowsePathElement> {
    private static final String BROWSE_PATH_SEPARATOR = "/";
    public static final String BROWSE_ELEMENT_SEPARATOR = ":";

    @Getter(PRIVATE)
    private final Iterable<BrowsePathElement> elements;

    public BrowsePath() {
        this(Collections.<BrowsePathElement>emptyList());
    }

    public BrowsePath(Iterable<BrowsePathElement> elements) {
        this.elements = copyOf(elements);
    }

    @Override
    public Iterator<BrowsePathElement> iterator() {
        return getElements().iterator();
    }

    public String asString() {
        return asString(this);
    }

    public BrowsePath concat(final BrowsePath browsePath) {
        return concat(this, browsePath);
    }

    public BrowsePath withNamespaceIndex(final int index) {
        return new BrowsePath(transform(this, new Function<BrowsePathElement, BrowsePathElement>() {
            public BrowsePathElement apply(@Nullable BrowsePathElement element) {
                return element.withNamespaceIndex(index);
            }
        }));
    }

    public static BrowsePath concat(final HasBrowsePath... paths) {
        BrowsePath result = new BrowsePath();

        for (final HasBrowsePath path : paths) {
            if (path != null && path.getBrowsePath() != null) {
                result = result.concat(path.getBrowsePath());
            }
        }

        return result;
    }

    public static BrowsePath concat(final BrowsePath path1, final BrowsePath path2) {
        Iterable<BrowsePathElement> deviceBrowsePath = path1.elements;
        Iterable<BrowsePathElement> valueBrowsePath = path2.elements;
        if (deviceBrowsePath == null) {
            deviceBrowsePath = Collections.emptyList();
        }
        if (valueBrowsePath == null) {
            valueBrowsePath = Collections.emptyList();
        }

        return new BrowsePath(Iterables.concat(deviceBrowsePath, valueBrowsePath));
    }

    public static String asString(BrowsePath browsePath) {
        if (browsePath == null) {
            return "";
        }

        return from(browsePath.getElements())
                .transform(new Function<BrowsePathElement, String>() {
                    public String apply(BrowsePathElement element) {
                        return element.namespaceName().or(valueOf(element.getNamespaceIndex())) + BROWSE_ELEMENT_SEPARATOR + element.getName();
                    }
                })
                .join(Joiner.on(BROWSE_PATH_SEPARATOR));
    }

    /**
     * Browse path: "index0:name0/index1:name0" or "namespace0:name0/namespace1:name1"
     */
    public static BrowsePath asBrowsePath(@Nullable Object input) {
        if (input == null) {
            return new BrowsePath(Collections.<BrowsePathElement>emptyList());
        }

        if (input instanceof BrowsePath) {
            return (BrowsePath) input;
        }

        return new BrowsePath(from(asList(input.toString().split(BROWSE_PATH_SEPARATOR)))
                .filter(new Predicate<String>() {
                    @Override
                    public boolean apply(@Nullable String s) {
                        return !isEmpty(s);
                    }
                })
                .transform(new Function<String, BrowsePathElement>() {
                    public BrowsePathElement apply(String name) {
                        final String[] names = name.split(BROWSE_ELEMENT_SEPARATOR);
                        if (names.length == 1) {
                            return new BrowsePathElement(0, names[0]);
                        } else if (names.length == 2) {
                            final Integer namespaceId = parseInt(names[0]);
                            if (namespaceId != null) {
                                return new BrowsePathElement(namespaceId, names[1]);
                            } else {
                                return new BrowsePathElement(names[0], names[1]);
                            }
                        }
                        throw new RuntimeException("Cannot parse " + name);
                    }
                }));
    }

}

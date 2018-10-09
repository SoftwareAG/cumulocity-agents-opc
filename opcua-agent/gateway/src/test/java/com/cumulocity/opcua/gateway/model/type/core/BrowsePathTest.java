package com.cumulocity.opcua.gateway.model.type.core;

import org.junit.Test;

import static com.cumulocity.opcua.gateway.model.type.core.BrowsePath.asBrowsePath;
import static com.cumulocity.opcua.gateway.model.type.core.BrowsePath.concat;
import static com.google.common.collect.Iterables.get;
import static org.assertj.core.api.Assertions.assertThat;

public class BrowsePathTest {
    @Test
    public void shouldParseString() {
        final BrowsePath browsePath = asBrowsePath("Objects/4:Boilers/4:Boiler #1/4:PipeX001/4:FTX001/4:Output");

        assertThat(browsePath).hasSize(6);
        assertThat(get(browsePath, 0)).isEqualTo(new BrowsePathElement(0, "Objects"));
        assertThat(get(browsePath, 1)).isEqualTo(new BrowsePathElement(4, "Boilers"));
        assertThat(get(browsePath, 2)).isEqualTo(new BrowsePathElement(4, "Boiler #1"));
        assertThat(get(browsePath, 3)).isEqualTo(new BrowsePathElement(4, "PipeX001"));
        assertThat(get(browsePath, 4)).isEqualTo(new BrowsePathElement(4, "FTX001"));
        assertThat(get(browsePath, 5)).isEqualTo(new BrowsePathElement(4, "Output"));
    }

    @Test
    public void shouldHaveDefaultNamespace() {
        final BrowsePath browsePath = asBrowsePath("Objects/Boilers");

        assertThat(browsePath).hasSize(2);
        assertThat(get(browsePath, 0)).isEqualTo(new BrowsePathElement(0, "Objects"));
        assertThat(get(browsePath, 1)).isEqualTo(new BrowsePathElement(0, "Boilers"));
    }

    @Test
    public void shouldConcat() {
        final BrowsePath browsePath = concat(asBrowsePath("Objects"), asBrowsePath("4:Boilers"));

        assertThat(browsePath).hasSize(2);
        assertThat(get(browsePath, 0)).isEqualTo(new BrowsePathElement(0, "Objects"));
        assertThat(get(browsePath, 1)).isEqualTo(new BrowsePathElement(4, "Boilers"));
    }
}

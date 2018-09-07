package com.cumulocity.opcua.gateway.repository.configuration;

import com.cumulocity.opcua.gateway.model.type.core.BrowsePath;
import com.cumulocity.opcua.platform.controller.configuration.PlatformObjectMapperConfiguration;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
public class RepositoryConfiguration {

    public static File findConfSubdirectory(String subfolderName) {
        final File home = new File(System.getProperty("user.home"), ".opcua");
        final File etc = new File("/etc/opcua");
        final File confDirectory;
        if (home.exists()) {
            confDirectory = home;
        } else if (etc.exists()) {
            confDirectory = etc;
        } else {
            confDirectory = new File(System.getProperty("java.io.tmpdir"));
        }

        final File dbDirectory = new File(confDirectory, subfolderName);
        if (!dbDirectory.exists()) {
            dbDirectory.mkdir();
        }
        return dbDirectory;
    }

    @Bean
    public static ObjectMapper objectMapper() {
        final ObjectMapper result = new ObjectMapper();
        PlatformObjectMapperConfiguration.configureObjectMapper(result);
        result.registerModule(browsePathModule());
        return result;
    }

    private static SimpleModule browsePathModule() {
        return new SimpleModule() {{
            addSerializer(BrowsePath.class, new JsonSerializer<BrowsePath>() {
                @Override
                public void serialize(BrowsePath value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    gen.writeString(BrowsePath.asString(value));
                }
            });
            addDeserializer(BrowsePath.class, new JsonDeserializer<BrowsePath>() {
                @Override
                public BrowsePath deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    return BrowsePath.asBrowsePath(p.getValueAsString());
                }
            });
        }};
    }
}

package com.cumulocity.opcua.configuration;

import com.cumulocity.opcua.persistance.repository.DBStore;
import com.cumulocity.opcua.platform.repository.configuration.PlatformProperties;
import com.cumulocity.opcua.platform.repository.configuration.PlatformProperties.DeviceBootstrapConfigurationProperties;
import com.cumulocity.opcua.platform.repository.configuration.PlatformProvider;
import com.cumulocity.sdk.client.Platform;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;

@Configuration
@IntegrationTest
@WebAppConfiguration
@ComponentScan("com.cumulocity.opcua")
public class TestConfiguration extends WebMvcConfigurerAdapter implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

    @Value("${platform.bootstrap.tenant}")
    private String tenant;
    @Value("${platform.bootstrap.user}")
    private String user;
    @Value("${platform.bootstrap.password}")
    private String password;

    private int port;

    @Override
    public void onApplicationEvent(final EmbeddedServletContainerInitializedEvent event) {
        port = event.getEmbeddedServletContainer().getPort();
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new SvensonConverter());
    }

    @Bean
    @Lazy
    @Scope(scopeName = SCOPE_PROTOTYPE, proxyMode = TARGET_CLASS)
    public PlatformProperties platformProperties() {
        return PlatformProperties.builder()
                .url("http://localhost:" + port)
                .forceInitialHost(false)
                .bootstrap(DeviceBootstrapConfigurationProperties.builder()
                        .tenant(tenant)
                        .user(user)
                        .password(password)
                        .build())
                .build();
    }

    @Bean
    @Lazy
    @Scope(scopeName = SCOPE_PROTOTYPE, proxyMode = TARGET_CLASS)
    public PlatformProvider platformProvider(final PlatformProperties platformProperties) {
        return new PlatformProvider(platformProperties);
    }

    @Bean
    @Lazy
    @Scope(scopeName = SCOPE_PROTOTYPE, proxyMode = TARGET_CLASS)
    public Platform bootstrapPlatform(PlatformProvider platformProvider) {
        return platformProvider.getBootstrapPlatform();
    }

    @Primary
    @Bean(destroyMethod = "close")
    public DBStore db() {
        return new DBStore() {
            protected DB db() {
                return DBMaker.memoryDB().make();
            }
        };
    }
    
}

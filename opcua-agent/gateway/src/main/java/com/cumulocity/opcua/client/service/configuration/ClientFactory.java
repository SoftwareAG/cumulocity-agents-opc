package com.cumulocity.opcua.client.service.configuration;

import com.cumulocity.opcua.client.model.Client;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.repository.configuration.ContextProvider;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.CertificateValidationListener;
import com.prosysopc.ua.PkiFileBasedCertificateValidator;
import com.prosysopc.ua.UserIdentity;
import com.prosysopc.ua.client.UaClient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.ApplicationType;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.core.UserTokenType;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.SecurityMode;
import org.opcfoundation.ua.transport.security.SecurityPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.Locale;

import static com.cumulocity.opcua.gateway.repository.configuration.RepositoryConfiguration.findConfSubdirectory;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.prosysopc.ua.PkiFileBasedCertificateValidator.ValidationResult.AcceptPermanently;
import static java.lang.reflect.Modifier.isStatic;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.opcfoundation.ua.core.UserTokenType.Certificate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientFactory implements Closeable {

    @Data
    @EqualsAndHashCode(of = "url")
    public static class Key {
        private final String url;
        private final Gateway gateway;
    }

    @Value("${client.applicationName:c8y_OPCUAGateway}")
    private String applicationName;

    @Value("${client.applicationIdentity:Anonymous}")
    private UserTokenType applicationIdentity;

    @Value("${client.applicationIdentityPrivateKeyPassword:cumulocity}")
    private String applicationPrivateKeyPassword;

    @Value("${client.applicationIdentityRenewingCertificate:true}")
    private boolean applicationRenewingCertificate;

    @Value("${client.userIdentity:Anonymous}")
    private UserTokenType userIdentity;

    @Value("${client.userIdentityCertificate:}")
    private String userCertificate;

    @Value("${client.userIdentityPrivateKey:}")
    private String userPrivateKey;

    @Value("${client.userIdentityPrivateKeyPassword:}")
    private String userPrivateKeyPassword;

    @Value("${client.userIdentityName:}")
    private String userIdentityName;

    @Value("${client.userIdentityPassword:}")
    private String userIdentityPassword;

    @Value("${client.url:}")
    private String defaultUrl;

    @Value("${client.autoReconnect:true}")
    private boolean autoReconnect;

    @Value("${client.organisation:cumulocity}")
    private String clientOrganisation;

    @Value("${client.securityMode:}")
    private String securityMode;

    @Value("${client.publishRequestFactor:}")
    private Double publishRequestFactor;

    private final LoadingCache<Key, Client> cache = newBuilder().removalListener(new RemovalListener<Key, Client>() {
        @Override
        public void onRemoval(RemovalNotification<Key, Client> removalNotification) {
            removalNotification.getValue().disconnect();
        }
    }).build(new CacheLoader<Key, Client>() {
        @SneakyThrows
        public Client load(final Key gatewayContext) {
            final UaClient client = new UaClient(getUrl(gatewayContext));
            final SecurityMode securityMode = createSecurityMode(gatewayContext.getGateway());
            client.setSecurityMode(securityMode);
            client.setAutoReconnect(autoReconnect);

            if (publishRequestFactor != null) {
                log.info("publishRequestFactor {} => {}", client.getPublishRequestFactor(), publishRequestFactor);
                client.setPublishRequestFactor(publishRequestFactor);
            }

            final ApplicationDescription applicationDescription = createApplicationDescription(gatewayContext, applicationName);
            client.setApplicationIdentity(createApplicationIdentity(applicationDescription, securityMode));

//            user identity
            final Optional<UserIdentity> userIdentity = createUserIdentity(gatewayContext.getGateway());
            if (userIdentity.isPresent()) {
                client.setUserIdentity(userIdentity.get());
            }

//            server certificate validator
            client.setCertificateValidator(createCertificateValidator(findConfSubdirectory("cert")));

            client.connect();
            return new Client(client);
        }

        @SneakyThrows
        private ApplicationIdentity createApplicationIdentity(ApplicationDescription applicationDescription, SecurityMode securityMode) {
            final ApplicationIdentity identity;
            final MessageSecurityMode messageSecurityMode = securityMode.getMessageSecurityMode();
            final boolean certificateRequired = messageSecurityMode.hasSigning() || messageSecurityMode.hasEncryption();

            if (Certificate.equals(applicationIdentity) || certificateRequired) {
                identity = ApplicationIdentity.loadOrCreateCertificate(
                        applicationDescription,
                        clientOrganisation,
                        applicationPrivateKeyPassword,
                        new File(findConfSubdirectory("cert"), "private"),
                        applicationRenewingCertificate
                );
            } else {
                identity = new ApplicationIdentity();
                identity.setApplicationDescription(applicationDescription);
            }
            return identity;
        }

        @SneakyThrows
        private Optional<UserIdentity> createUserIdentity(Gateway gateway) {
            if (isNotBlank(gateway.getUserIdentityName()) && isNotBlank(gateway.getUserIdentityPassword())) {
                return of(new UserIdentity(gateway.getUserIdentityName(), gateway.getUserIdentityPassword()));
            }
            if (isNotBlank(userIdentityName) || isNotBlank(userIdentityPassword)) {
                return of(new UserIdentity(userIdentityName, userIdentityPassword));
            }
            if (isNotBlank(userCertificate) || isNotBlank(userPrivateKey)) {
                return of(new UserIdentity(new File(userCertificate), new File(userPrivateKey), userPrivateKeyPassword));
            }
            return absent();
        }

        @SneakyThrows
        private SecurityMode createSecurityMode(Gateway gateway) {
            if (isNotBlank(gateway.getSecurityMode())) {
                final Optional<SecurityMode> securityMode = findStaticValue(SecurityMode.class, gateway.getSecurityMode());
                if (securityMode.isPresent()) {
                    return securityMode.get();
                }
            }

            if (isNotBlank(gateway.getSecurityPolicy()) && isNotBlank(gateway.getMessageSecurityMode())) {
                final Optional<SecurityPolicy> securityPolicy = findStaticValue(SecurityPolicy.class, gateway.getSecurityMode());
                if (securityPolicy.isPresent()) {
                    return new SecurityMode(securityPolicy.get(), MessageSecurityMode.valueOf(gateway.getMessageSecurityMode()));
                }
            }
            if (isNotBlank(securityMode)) {
                final Optional<SecurityMode> securityPolicy = findStaticValue(SecurityMode.class, securityMode);
                if (securityPolicy.isPresent()) {
                    return securityPolicy.get();
                }
            }
            return SecurityMode.NONE;
        }

        private <T> Optional<T> findStaticValue(Class<T> securityModeClass, String value) throws IllegalAccessException {
            final Field[] fields = securityModeClass.getFields();
            for (final Field field : fields) {
                if (isStatic(field.getModifiers())) {
                    if (field.getName().equals(value)) {
                        field.setAccessible(true);
                        return of((T) field.get(null));
                    }
                }
            }
            return absent();
        }

        private ApplicationDescription createApplicationDescription(Key key, String applicationName) {
            // *** Application Description is sent to the server
            final ApplicationDescription appDescription = new ApplicationDescription();
            appDescription.setApplicationName(new LocalizedText(applicationName, Locale.ENGLISH));
            appDescription.setApplicationUri(createApplicationUri(key.getGateway(), applicationName));
            appDescription.setProductUri(createProductId(key.getGateway(), applicationName));
            appDescription.setApplicationType(ApplicationType.Client);
            return appDescription;
        }

        private String createProductId(final Gateway gateway, final String name) {
            return Optional.fromNullable(gateway.getProductUri()).or(new Supplier<String>() {
                @Override
                public String get() {
                    return "urn:cumulocity.com:UA:" + name;
                }
            });
        }

        // 'localhost' (all lower case) in the URI is converted to the actual
        // host name of the computer in which the application is run
        private String createApplicationUri(final Gateway gateway, final String name) {
            return Optional.fromNullable(gateway.getApplicationUri()).or(new Supplier<String>() {
                @Override
                public String get() {
                    return "urn:localhost:UA:" + name;
                }
            });
        }
    });

    private String getUrl(Key gatewayContext) {
        if (isNotBlank(gatewayContext.getUrl())) {
            return gatewayContext.getUrl();
        }
        return defaultUrl;
    }

    private PkiFileBasedCertificateValidator createCertificateValidator(File certs) {
        final PkiFileBasedCertificateValidator validator = new PkiFileBasedCertificateValidator(certs.getAbsolutePath());
        validator.setValidationListener(new CertificateValidationListener() {
            public PkiFileBasedCertificateValidator.ValidationResult onValidate(final Cert cert, final ApplicationDescription applicationDescription, final EnumSet<PkiFileBasedCertificateValidator.CertificateCheck> enumSet) {
                return AcceptPermanently;
            }
        });
        return validator;
    }

    @SneakyThrows
    public Client client() {
        final Gateway gateway = ContextProvider.get(Gateway.class).get();
        final Key key = new Key(gateway.getUrl(), gateway);
        final Client client = cache.get(key);
        if (!client.isConnected()) {
            cache.refresh(key);
        }
        return client;
    }

    @Override
    public void close() {
        cache.cleanUp();
    }
}

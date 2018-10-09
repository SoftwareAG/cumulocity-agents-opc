package com.cumulocity.opcua.mock.opcua.configuration;

import com.prosysopc.ua.CertificateValidationListener;
import com.prosysopc.ua.PkiFileBasedCertificateValidator;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.UaServerException;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.UserTokenPolicy;
import org.opcfoundation.ua.transport.security.Cert;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import static com.prosysopc.ua.ApplicationIdentity.loadOrCreateCertificate;
import static com.prosysopc.ua.PkiFileBasedCertificateValidator.ValidationResult.AcceptPermanently;
import static com.prosysopc.ua.UaApplication.Protocol.Https;
import static com.prosysopc.ua.UaApplication.Protocol.OpcTcp;
import static org.assertj.core.util.Files.temporaryFolderPath;
import static org.opcfoundation.ua.core.ApplicationType.Server;

@Configuration
@ComponentScan
public class ServerConfiguration {

    private boolean secured;

    @Bean(destroyMethod = "close")
    public UaServer createServer() throws Exception {
        final String applicationName = "IntegrationTestServer";
        final UaServer server = new UaServer();

        ApplicationDescription appDescription = new ApplicationDescription();
        appDescription.setApplicationName(new LocalizedText(applicationName + "@localhost"));
        appDescription.setApplicationUri("urn:localhost:OPCUA:" + applicationName);
        appDescription.setProductUri("urn:cumulocity.com:OPCUA:" + applicationName);
        appDescription.setApplicationType(Server);

        if (secured) {
            setUpSecuredApplicationIdentity(server, appDescription);
        } else {
            setUpUnsercuredApplicationIdentity(server, appDescription);
        }

        server.setPort(OpcTcp, 52520);
        server.setPort(Https, 52443);
        server.setServerName("OPCUA/IntegrationTestServer");

        server.init();
        server.start();

        return server;
    }

    private void setUpUnsercuredApplicationIdentity(UaServer server, ApplicationDescription appDescription) throws SecureIdentityException, IOException, UaServerException {
        final PkiFileBasedCertificateValidator validator = new PkiFileBasedCertificateValidator(temporaryFolderPath());
        validator.setValidationListener(new CertificateValidationListener() {
            @Override
            public PkiFileBasedCertificateValidator.ValidationResult onValidate(final Cert cert, final ApplicationDescription applicationDescription, final EnumSet<PkiFileBasedCertificateValidator.CertificateCheck> enumSet) {
                return AcceptPermanently;
            }
        });
        server.setCertificateValidator(validator);
        File privatePath = new File(validator.getBaseDir(), "private");
        server.setApplicationIdentity(loadOrCreateCertificate(appDescription, "Cumulocity", "opcua", privatePath, true));
        server.addUserTokenPolicy(UserTokenPolicy.ANONYMOUS);
        server.addUserTokenPolicy(UserTokenPolicy.SECURE_USERNAME_PASSWORD);
    }

    private void setUpSecuredApplicationIdentity(UaServer server, ApplicationDescription appDescription) throws SecureIdentityException, IOException, UaServerException {
        final PkiFileBasedCertificateValidator validator = new PkiFileBasedCertificateValidator(getCertFilePath());
        server.setCertificateValidator(validator);
        File privatePath = new File(getCertFilePath(), "private");

        server.setApplicationIdentity(loadOrCreateCertificate(appDescription, "cumulocity", "cumulocity", privatePath, true));
        server.addUserTokenPolicy(UserTokenPolicy.SECURE_CERTIFICATE);
        server.addUserTokenPolicy(UserTokenPolicy.SECURE_CERTIFICATE_BASIC256);
    }

    private String getCertFilePath() {
        final File home = new File(System.getProperty("user.home"), ".opcua-server");
        final File cert = new File(home, "cert");
        cert.mkdirs();
        return cert.getAbsolutePath();
    }

}

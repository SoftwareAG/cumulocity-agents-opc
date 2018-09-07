package com.cumulocity.opcua.simulator;

import com.cumulocity.opcua.gateway.model.type.core.BrowsePath;
import com.cumulocity.opcua.gateway.service.utils.Consumer;
import com.cumulocity.opcua.mock.opcua.ServerBuilder;
import com.prosysopc.ua.nodes.UaInstance;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.core.Attributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ThreadLocalRandom;

import static com.cumulocity.opcua.gateway.model.type.core.BrowsePath.asBrowsePath;

@Slf4j
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.cumulocity.opcua")
public class Simulator implements CommandLineRunner {
    public static void main(String... args) {
        SpringApplication.run(Simulator.class, args);
    }

    @Autowired
    private ServerBuilder server;

    @Override
    public void run(String... strings) throws Exception {
//        BrowsePath browsePath = asBrowsePath("Boilers/Boiler #1/PipeX001/FTX001/Output");

        log.info("opc.tcp://localhost:52520/OPCUA/IntegrationTestServer");

        for (int i = 0; i < 1; i ++) {
            for (int j = 0; j <= 50; j ++) {
                BrowsePath browsePath = asBrowsePath("Boiler #" + i + "/Output" + j);
                log.info("{}", browsePath.asString());
                server.createObjectNode(browsePath, new Consumer<UaInstance>() {
                    public void apply(UaInstance object) {
                        generateData(object);
                    }
                });
            }
        }

        server.start();
    }

    @SneakyThrows
    private void generateData(UaInstance object) {
        final int value = ThreadLocalRandom.current().nextInt();
        final Variant variant = new Variant(value);
        final DataValue dataValue = new DataValue();
        dataValue.setValue(variant);
        object.writeAttribute(Attributes.Value, dataValue);
    }
}

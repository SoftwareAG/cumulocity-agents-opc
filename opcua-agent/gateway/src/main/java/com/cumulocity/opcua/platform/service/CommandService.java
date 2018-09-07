package com.cumulocity.opcua.platform.service;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.annotations.RunWithinContext;
import com.cumulocity.opcua.gateway.model.client.GatewayConfigErrorEvent;
import com.cumulocity.opcua.gateway.model.core.ConfigEventType;
import com.cumulocity.opcua.gateway.model.core.Credentials;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.repository.configuration.ContextProvider;
import com.cumulocity.opcua.platform.model.Command;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommandService {

    private final Map<GId, ScheduledFuture> executors = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    @Getter(AccessLevel.PACKAGE)
    private final List<Command> commands = new CopyOnWriteArrayList<>();
    private final Executor worker;
    private final AutowireCapableBeanFactory autowireCapableBeanFactory;
    private final ApplicationEventPublisher eventPublisher;

    @RunWithinContext
    public void queueForExecution(final Credentials credentials, final Command command) {
        final Optional<Gateway> gatewayOptional = ContextProvider.get(Gateway.class);

        if (gatewayOptional.isPresent()) {
            final Gateway gateway = gatewayOptional.get();
            if (gateway.getTransmitRateInSeconds() == null) {
                executeInDifferentThead(gateway, command);
            } else {
                synchronized (this) {
                    commands.add(command);

                    if (!executors.containsKey(gateway.getId())) {
                        executors.put(gateway.getId(), scheduler.schedule(new Runnable() {
                            public void run() {
                                executePendingCommands(gateway);
                            }
                        }, gateway.getTransmitRateInSeconds(), TimeUnit.SECONDS));
                    }
                }
            }
        }
    }

    @PreDestroy
    public void destroy() throws Exception {
        try {
            for (ScheduledFuture executor : executors.values()) {
                executor.cancel(true);
            }
        } finally {
            executors.clear();
        }
    }

    @RunWithinContext
    private void executePendingCommands(final Gateway gateway) {
        final List<Command> localList;
        synchronized (this) {
            localList = new ArrayList<>(commands);
            commands.clear();
            executors.remove(gateway.getId());
        }

        for (final Command command : localList) {
            executeInDifferentThead(gateway, command);
        }
    }

    private void executeInDifferentThead(final Gateway gateway, final Command command) {
        worker.execute(new Runnable() {
            public void run() {
                execute(gateway, command);
            }
        });
    }

    @RunWithinContext
    private void execute(final Gateway gateway, Command command) {
        try {
            autowireCapableBeanFactory.autowireBean(command);
            command.execute();
        } catch (final Exception ex) {
            log.error(ex.getMessage(), ex);
            eventPublisher.publishEvent(new GatewayConfigErrorEvent(gateway, new ConfigEventType(ex.getMessage())));
        }
    }
}

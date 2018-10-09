package com.cumulocity.opcua.platform.service

import com.cumulocity.opcua.gateway.model.gateway.Gateway
import com.cumulocity.opcua.platform.model.Command
import org.mockito.Mockito
import org.opcfoundation.ua.utils.CurrentThreadExecutor
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationEventPublisher
import spock.lang.Specification

import static com.cumulocity.model.idtype.GId.asGId

class CommandServiceTest extends Specification {

    def commandService = new CommandService(new CurrentThreadExecutor(), Mockito.mock(AutowireCapableBeanFactory.class), Mockito.mock(ApplicationEventPublisher.class));

    def "should delay command execution"() {
        given:
        def delay = 1;

        def gateway1 = Gateway.gateway().tenant("management").name("user").password("pass").id(asGId("1234")).build()
        def gateway2 = Gateway.gateway().tenant("test").name("user").password("pass").id(asGId("5678")).transmitRateInSeconds(delay).pollingRateInSeconds(100).build()

        def syncCommand = Mock(Command);
        def asyncCommand = Mock(Command);

        when:
        commandService.queueForExecution(gateway1, syncCommand);
        commandService.queueForExecution(gateway2, asyncCommand);

        then:
        1 * syncCommand.execute()
        0 * asyncCommand.execute()

        when:
        commandService.executors[gateway2.getId()].get()

        then:
        1 * asyncCommand.execute()
    }
}

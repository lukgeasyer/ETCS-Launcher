package ebd.etcsLauncher.backend.api.webSocket

import ebd.etcsLauncher.backend.api.controller.ProcessManagerController
import ebd.etcsLauncher.backend.model.processManager.ProcessManager
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleNames
import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.lang.Nullable
import org.springframework.messaging.converter.StringMessageConverter
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandler
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import spock.lang.Specification

import java.lang.reflect.Type
import java.nio.file.Path
import java.util.concurrent.CountDownLatch

import static ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemLogic.getJarFolder

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class RunningModulesWebSocketSpecification extends Specification {
    private final Logger logger = LoggerFactory.getLogger("WebSocketTest")
    @LocalServerPort
    private int port
    private TestRestTemplate restTemplate = new TestRestTemplate()
    @Autowired
    ProcessManager processManager
    @Autowired
    ProcessManagerController processManagerController

    def fillWithAvailable() {
        Path resourceDirectory = Path.of("src").resolve("test").resolve("resources").resolve(getJarFolder())
        Path tempTestDirectory = Path.of(getJarFolder())
        FileUtils.copyDirectory(resourceDirectory.toFile(), tempTestDirectory.toFile())
        restTemplate.postForEntity("http://localhost:$port/moduleManager/availableModules/scanFileSystem", null, String)
    }

    def getModulesToRun() {
        def modulesToRun = [:]
        modulesToRun[ETCSModuleNames.ModuleName.CORE.name()] = "1.3.4"

        return modulesToRun
    }

    def getStompSessionHandler(ArrayList<String> receivedMessages, CountDownLatch startLatch, CountDownLatch lastMessageLatch,
                               CountDownLatch stopMessageReceibedLatch) {
        return new StompSessionHandler() {
            @Override
            void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                logger.info(session.subscribe("/processManager/running/CORE", this) as String)
                startLatch.countDown()
            }

            @Override
            void handleException(StompSession session, @Nullable StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                logger.error(exception as String)
            }

            @Override
            void handleTransportError(StompSession session, Throwable exception) {
                logger.error(exception as String)
            }

            @Override
            Type getPayloadType(StompHeaders headers) {
                return String.class
            }

            @Override
            void handleFrame(StompHeaders headers, @Nullable Object payload) {
                if (payload instanceof String) {
                    String message = (String) payload
                    if (message.contains("This is the virtual environment for the ETCS@EBD project. Enter 'start' to begin.")) {
                        lastMessageLatch.countDown()
                    }
                    if (message.contains("STOPPED")) {
                        stopMessageReceibedLatch.countDown()
                    }
                    receivedMessages.add(message)
                } else {
                    logger.error("Received unknown payload type: " + payload)
                }
            }
        }
    }

    def "specify correct websocket output - for core module"() {
        given:
        def receivedMessages = new ArrayList<String>()
        CountDownLatch webSocketConnectionLatch = new CountDownLatch(1)
        CountDownLatch lastMessageReceivedLatch = new CountDownLatch(1)
        CountDownLatch stopMessageReceivedLatch = new CountDownLatch(1)
        fillWithAvailable()
        def httpHeaders = new HttpHeaders()
        httpHeaders.setContentType(MediaType.APPLICATION_JSON)
        def requestEntity = new HttpEntity<>(getModulesToRun(), httpHeaders)
        def webSocketURI = "ws://localhost:$port/processManager/webSocket"

        WebSocketClient coreClient = new StandardWebSocketClient()
        WebSocketStompClient stompClient = new WebSocketStompClient(coreClient)
        stompClient.setMessageConverter(new StringMessageConverter())
        StompSessionHandler sessionHandler = getStompSessionHandler(receivedMessages, webSocketConnectionLatch, lastMessageReceivedLatch, stopMessageReceivedLatch)
        stompClient.connectAsync(webSocketURI, sessionHandler)

        webSocketConnectionLatch.await()

        when:
        def response = restTemplate.exchange("http://localhost:$port/processManager/run", HttpMethod.POST, requestEntity, String)
        lastMessageReceivedLatch.await()
        restTemplate.postForEntity("http://localhost:$port/processManager/stop", null, String)
        stopMessageReceivedLatch.await()

        then:
        response.statusCode == HttpStatus.NO_CONTENT
        receivedMessages.get(0) == "STARTED"
        receivedMessages.get(1).contains("Created logback-spring.xml from default resource.")
        receivedMessages.last == "STOPPED"
        stompClient.stop()
    }

    def cleanup() {
        Path.of(getJarFolder()).toFile().deleteDir()
    }
}

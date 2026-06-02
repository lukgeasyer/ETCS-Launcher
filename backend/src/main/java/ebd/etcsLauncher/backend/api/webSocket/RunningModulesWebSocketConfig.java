package ebd.etcsLauncher.backend.api.webSocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures a websocket for the output of running {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}s.
 *
 * @author Lukas Geyer
 */
@Configuration
@EnableWebSocketMessageBroker
public class RunningModulesWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Defines websocket endpoints to which a client can connect to.
     *
     * @param registry
     *         a StompEndpointRegistry initialized by the SpringBoot application
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/processManager/webSocket").setAllowedOrigins("*");
    }

    /**
     * Messages can be sent to clients or the server that have the destinationPrefixes as prefix.
     *
     * @param config
     *         a MessageBrokerRegistry initialized by the SpringBoot application
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setPreservePublishOrder(true);
        config.enableSimpleBroker("/processManager/running/");
    }

}

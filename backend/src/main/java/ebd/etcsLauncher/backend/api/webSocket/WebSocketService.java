package ebd.etcsLauncher.backend.api.webSocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Receives input from the internal java program and sends it to the websocket endpoint.
 *
 * @author Lukas Geyer
 */
@Service
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendMessageToAddress(String address, String message) {
        messagingTemplate.convertAndSend(address, message);
    }

}

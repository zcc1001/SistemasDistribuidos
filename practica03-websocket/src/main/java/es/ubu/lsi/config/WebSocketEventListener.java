package es.ubu.lsi.config;

import es.ubu.lsi.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

  private final MessageSendingOperations messageSendingOperations;

  @Autowired
  public WebSocketEventListener(MessageSendingOperations messageSendingOperations) {
    this.messageSendingOperations = messageSendingOperations;
  }

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String username = (String) headerAccessor.getSessionAttributes().get("username");
    if (username != null) {
      var chatMessage = new Message();
      chatMessage.setFrom(username);
      chatMessage.setText("LEAVE");
      messageSendingOperations.convertAndSend("/topic/public", chatMessage);
    }
  }
}

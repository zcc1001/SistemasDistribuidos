package es.ubu.lsi.controller;

import es.ubu.lsi.model.Message;
import es.ubu.lsi.model.User;
import es.ubu.lsi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

  private final UserService userService;

  /**
   * Cosntructor que injecta beans pasados
   *
   * @param userService
   */
  @Autowired
  public ChatController(UserService userService) {
    this.userService = userService;
  }

  /**
   * Endpoint para registra un usuario en el chat
   *
   * @param message
   * @param headerAccessor
   * @return
   */
  @MessageMapping("/chat/register")
  @SendTo("/topic/public")
  public Message register(
      @Payload final Message message, SimpMessageHeaderAccessor headerAccessor) {
    User user = userService.findUser(message.getFrom(), message.getText());
    if (user == null) {
      throw new RuntimeException("invalid User");
    }
    Message responseMessage = new Message();
    responseMessage.setFrom(message.getFrom());
    responseMessage.setFrom_level(user.getLevel().toString());
    responseMessage.setText("REGISTER");

    return responseMessage;
  }

  /**
   * Endpoint para enviar mensajes al chat
   *
   * @param message
   * @return
   */
  @MessageMapping("/chat/send")
  @SendTo("/topic/public")
  public Message sendMessage(@Payload final Message message) {
    return message;
  }
}

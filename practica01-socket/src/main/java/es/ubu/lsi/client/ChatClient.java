package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

public interface ChatClient {
  boolean start();

  void sendMessage(ChatMessage chatMessage);

  void disconnect();

}

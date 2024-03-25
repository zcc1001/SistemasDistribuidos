package es.ubu.lsi.server;

import es.ubu.lsi.common.ChatMessage;

public interface ChatServer {
  void starup();

  void shutdown();

  void broadcast(ChatMessage chatMessage);

  void remove(int id);
}

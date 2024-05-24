package es.ubu.lsi.model;

public class Message {
  private String from;
  private String from_id;
  private String from_level;
  private String text;

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getFrom_id() {
    return from_id;
  }

  public void setFrom_id(String from_id) {
    this.from_id = from_id;
  }

  public String getFrom_level() {
    return from_level;
  }

  public void setFrom_level(String from_level) {
    this.from_level = from_level;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}

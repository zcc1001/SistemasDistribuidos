package es.ubu.lsi.model;

public class User {
  private String name;
  private String password;
  private LEVEL level;

  public enum LEVEL {
    LEVEL_1,
    LEVEL_2,
    LEVEL_3
  }

  public User(String name, String password, int level) {
    this.name = name;
    this.password = password;
    switch (level) {
      case 1 -> this.level = LEVEL.LEVEL_1;
      case 2 -> this.level = LEVEL.LEVEL_2;
      case 3 -> this.level = LEVEL.LEVEL_3;
      default -> this.level = LEVEL.LEVEL_3;
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public LEVEL getLevel() {
    return level;
  }

  public void setLevel(LEVEL level) {
    this.level = level;
  }

  @Override
  public String toString() {
    return "User{"
        + "name='"
        + name
        + '\''
        + ", password='"
        + password
        + '\''
        + ", nivel="
        + level
        + '}';
  }
}

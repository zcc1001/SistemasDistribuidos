package es.ubu.lsi.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import es.ubu.lsi.model.User;
import jakarta.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final ResourceLoader resourceLoader;
  private final Map<String, User> users = new HashMap<>();

  @Autowired
  public UserService(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @PostConstruct
  public void init() throws IOException, CsvException {
    Resource resource = resourceLoader.getResource("classpath:users_db.csv");
    File csv = resource.getFile();
    Reader reader = new InputStreamReader(new FileInputStream(csv));

    // Parse CSV data
    try (CSVReader csvReader = new CSVReaderBuilder(reader).build()) {
      String[] values = null;
      while ((values = csvReader.readNext()) != null) {
        User user = new User(values[0], values[1], Integer.parseInt(values[2]));
        users.put(user.getName(), user);
      }
    }
  }

  public User findUser(String from, String text) {
    User user = users.get(from);
    if (user != null && user.getPassword().equals(text)) {
      return user;
    }
    return null;
  }
}

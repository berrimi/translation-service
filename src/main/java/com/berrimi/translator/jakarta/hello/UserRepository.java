
package com.berrimi.translator.jakarta.hello;

import java.util.concurrent.ConcurrentHashMap;

public class UserRepository {
  private static ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

  public static boolean register(User user) {
    if (users.containsKey(user.getUsername()))
      return false;
    users.put(user.getUsername(), user);
    return true;
  }

  public static boolean login(String username, String password) {
    User u = users.get(username);
    return u != null && u.getPassword().equals(password);
  }
}

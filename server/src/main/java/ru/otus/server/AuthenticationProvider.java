package ru.otus.server;

public interface AuthenticationProvider {
    String authenticateUser(String login, String password);

    boolean checkUserAdmin(String username);

    boolean registerUser(String login, String password, String username);

    void deleteUser(String username);
}

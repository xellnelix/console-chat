package ru.otus.server;

public interface AuthenticationProvider {
	String getUsernameByLoginAndPassword(String login, String username);

	boolean register(String login, String password, String username);
}
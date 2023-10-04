package ru.otus.server;

public interface AuthenticationProvider {
	String getUsernameByLoginAndPassword(String login, String username);
	Role getRoleByUsername(String username);

	boolean register(String login, String password, String username, Role role);
	void kick(String username);
}
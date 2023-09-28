package ru.otus.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class InMemoryAuthenticationProvider implements AuthenticationProvider {
	private final List<User> users;

	public InMemoryAuthenticationProvider() {
		this.users = new ArrayList<>();
	}

	@Override
	public String getUsernameByLoginAndPassword(String login, String password) {
		for (User user : users) {
			if (Objects.equals(user.getPassword(), password) && Objects.equals(user.getLogin(), login)) {
				return user.getUsername();
			}
		}
		return null;
	}

	@Override
	public synchronized boolean register(String login, String password, String username, Role role) {
		for (User user : users) {
			if (Objects.equals(user.getUsername(), username) && Objects.equals(user.getLogin(), login)) {
				return false;
			}
		}
		users.add(new User(login, password, username, role));
		return true;
	}

	@Override
	public synchronized void kick(String username) {
		for (int i = 0; i < users.size(); i++) {
			if (Objects.equals(users.get(i).getUsername(), username)) {
				users.remove(users.get(i));
			}
		}
	}

	@Override
	public Role getRoleByUsername(String username) {
		for (User user : users) {
			if (Objects.equals(user.getUsername(), username)) {
				return user.getRole();
			}
		}
		return null;
	}
}

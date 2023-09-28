package ru.otus.server;

public class User {
	private final String login;
	private final String password;
	private final String username;
	private Role role;

	public Role getRole() {
		return role;
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public User(String login, String password, String username, Role role) {
		this.login = login;
		this.password = password;
		this.username = username;
		this.role = role;
	}


}

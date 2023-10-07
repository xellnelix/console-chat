package ru.otus.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Server {
	private final int port;
	private final List<ClientHandler> clients;
	private static final String GET_ADMIN = "select username from chat_user cu join user_role ur on cu.role_id = ur.id and ur.role = 'ADMIN'";
	private static final String GET_USER_AUTH = "select username from chat_user where login = ? and password = ?";
	private static final String GET_USER_BY_USERNAME = "select * from chat_user where username = ?";
	private static final String GET_USER = "select login, username from chat_user where login = ? or username = ?";
	private static final String REG_USER = "insert into chat_user (login, password, username, role_id) values (?, ?, ?, 2)";
	private static final String DELETE_USER = "delete from chat_user where username = ?";
	private static final String DB_URL = "jdbc:postgresql://localhost/console-chat";
	private static final String DB_LOGIN = "postgres";
	private static final String DB_PASSWORD = "postgres";
	private final Connection databaseConnection = DriverManager.getConnection(DB_URL, DB_LOGIN, DB_PASSWORD);
	static ExecutorService thread = Executors.newFixedThreadPool(8);


	public Server(int port) throws SQLException {
		this.port = port;
		clients = new ArrayList<>();
	}

	public void start() {
		try (ServerSocket server = new ServerSocket(port)) {
			System.out.println("Сервер запущен на порту " + port);
			while (!server.isClosed()) {
				Socket client = server.accept();
				thread.execute(new ClientHandler(client, this));
			}
		} catch (IOException e) {
			thread.shutdown();
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public synchronized void addClient(ClientHandler clientHandler) {
		clients.add(clientHandler);
		broadcastMessage(clientHandler.getUsername() + " подключился!", clientHandler);
	}

	public synchronized void removeClient(ClientHandler clientHandler) {
		clients.remove(clientHandler);
		broadcastMessage(clientHandler.getUsername() + " отключен!", clientHandler);
	}

	public synchronized void broadcastMessage(String message, ClientHandler clientHandler) {
		for (ClientHandler client : clients) {
			if (!client.getUsername().equals(clientHandler.getUsername())) {
				client.sendMessage(clientHandler.getUsername() + ": " + message);
			}
		}
	}

	public synchronized void privateMessage(String user, String message, ClientHandler clientHandler) {
		for (ClientHandler client : clients) {
			if (client.getUsername().equals(user)) {
				client.sendMessage("Message from " + clientHandler.getUsername() + ": " + message);
			}
		}
	}

	public synchronized List<String> getUserList() {
		return clients.stream()
				.map(ClientHandler::getUsername)
				.collect(Collectors.toList());
	}

	public synchronized void kickClient(String username, ClientHandler clientHandler) {
		for (int i = 0; i < clients.size(); i++) {
				if (clients.get(i).getUsername().equals(username)) {
					deleteUser(username);
					clients.get(i).sendMessage("Вы были исключены администратором");
					clients.get(i).disconnect();
				}
			}
	}

	public String authenticateUser(String login, String password) {
		try (ResultSet rs = sqlReadParams(GET_USER_AUTH, login, password)) {
			if(rs.next()) {
				return rs.getString("username");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean checkUserAdmin(String username) {
		try (ResultSet rs = sqlQuery(GET_ADMIN)) {
			while(rs.next()) {
				if (rs.getString("username").equals(username)) {
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean registerUser(String login, String password, String username) {
		try (ResultSet rs = sqlReadParams(GET_USER, login, username)) {
			if(rs.next()) {
				return false;
			}
			if (sqlModifyParams(REG_USER, login, password, username) != 0) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void deleteUser(String username) {
		try (ResultSet rs = sqlReadParams(GET_USER_BY_USERNAME, username)) {
			while(rs.next()) {
				sqlModifyParams(DELETE_USER, username);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private ResultSet sqlQuery(String query) {
		try {
			PreparedStatement ps = databaseConnection.prepareStatement(query);
			return ps.executeQuery();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private ResultSet sqlReadParams(String query, String... params){
		try {
			PreparedStatement ps = databaseConnection.prepareStatement(query);
			for (int i = 0; i < params.length; i++) {
				ps.setString(i + 1, params[i]);
			}
			return ps.executeQuery();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private int sqlModifyParams(String query, String... params) {
		try {
			PreparedStatement ps = databaseConnection.prepareStatement(query);
			for (int i = 0; i < params.length; i++) {
				ps.setString(i + 1, params[i]);
			}
			return ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}


}

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
	static ExecutorService thread = Executors.newFixedThreadPool(8);
	private final JdbcAuthenticationProvider database;


	public Server(int port, JdbcAuthenticationProvider database) throws SQLException {
		this.port = port;
		clients = new ArrayList<>();
		this.database = database;
	}

	public JdbcAuthenticationProvider getDatabase() {
		return database;
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
					database.deleteUser(username);
					clients.get(i).sendMessage("Вы были исключены администратором");
					clients.get(i).disconnect();
				}
			}
	}
}

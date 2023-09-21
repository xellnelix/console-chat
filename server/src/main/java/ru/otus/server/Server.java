package ru.otus.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
	private final int port;
	private final List<ClientHandler> clients;

	public Server(int port) {
		this.port = port;
		clients = new ArrayList<>();
	}

	public void start() {
		try (ServerSocket server = new ServerSocket(port)) {
			System.out.println("Сервер запущен на порту " + port);
			while (!server.isClosed()) {
				Socket client = server.accept();
				new ClientHandler(client, this);
			}
		} catch (IOException e) {
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
}

package ru.otus.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        clients = new ArrayList<>();
    }

    public void start() {
        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                System.out.println("Сервер запущен на порту " + port);
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
        broadcastMessage("Клиент: " + clientHandler.getUsername() + " подключился!");
    }

    public synchronized void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMessage("Клиент: " + clientHandler.getUsername() + " отключен!");
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public synchronized void privateMessage(String user, String message) {
        for (ClientHandler client : clients) {
            System.out.println(client.getUsername());
            System.out.println(client.getUsername().equals(user));
            if (client.getUsername().equals(user)) {
                client.sendMessage(message);
            }
        }
    }
}

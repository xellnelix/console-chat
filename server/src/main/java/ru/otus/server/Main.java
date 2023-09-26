package ru.otus.server;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(8080, new InMemoryAuthenticationProvider());
        server.start();
    }
}

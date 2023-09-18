package ru.otus.server;

public class Main {
    public static void main(String[] args) {
        int port = 8085;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }
        Server server = new Server(port);
        server.start();
    }
}

package ru.otus.server;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            Server server = new Server(8080, new JdbcAuthenticationProvider());
            server.start();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}

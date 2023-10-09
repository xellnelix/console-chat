package ru.otus.server;

import java.sql.*;

public class JdbcAuthenticationProvider implements AuthenticationProvider {
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

    public JdbcAuthenticationProvider() throws SQLException {
    }

    public String authenticateUser(String login, String password) {
        try (ResultSet rs = sqlReadParams(GET_USER_AUTH, login, password)) {
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean checkUserAdmin(String username) {
        try (ResultSet rs = sqlQuery(GET_ADMIN)) {
            while (rs.next()) {
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
            if (rs.next()) {
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
            while (rs.next()) {
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

    private ResultSet sqlReadParams(String query, String... params) {
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

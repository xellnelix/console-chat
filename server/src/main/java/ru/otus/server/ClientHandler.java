package ru.otus.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private final DataInputStream in;
    private final DataOutputStream out;
    private String username;

    public String getUsername() {
        return username;
    }

    @Override
    public void run() {
        try {
            authenticateUser(server);
			communicateWithUser(server);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    private void authenticateUser(Server server) throws IOException {
        while (username == null) {
            String message = in.readUTF();
            String[] args = message.replaceAll("\\s+", " ").split(" ");
//			if (args.length != 3) {
//				out.writeUTF("Некорректное число аргументов");
//				continue;
//			}
            String command = args[0];
            switch (command) {
                case "/auth": {
                    String login = args[1];
                    String password = args[2];
                    String username = server.getDatabase().authenticateUser(login, password);
                    if (username == null || username.isBlank()) {
                        sendMessage("Указан неверный логин/пароль");
                    } else {
                        this.username = username;
                        sendMessage(username + ", добро пожаловать в чат!");
                        server.addClient(this);
                    }
                    break;
                }
                case "/register": {
                    String login = args[1];
                    String nick = args[2];
                    String password = args[3];
                    boolean isRegistered = server.getDatabase().registerUser(login, password, nick);
                    if (!isRegistered) {
                        sendMessage("Указаный логин/никнейм уже заняты");
                    } else {
                        this.username = nick;
                        sendMessage(nick + ", добро пожаловать в чат!");
                        server.addClient(this);
                    }
                    break;
                }
                default: {
                    sendMessage("Сперва нужно авторизоваться");
                }
            }
        }
    }

    private void communicateWithUser(Server server) throws IOException {
        while (socket.isConnected()) {
            try {
                String message = in.readUTF();
                if (message.startsWith("/")) {
                    if (message.equals("/exit")) {
                        break;
                    }
                    if (message.startsWith("/w")) {
                        String[] personalMessage = message.replaceAll("\\s+", " ").split(" ");
                        if (personalMessage.length != 3) {
                            out.writeUTF("Некорректное число аргументов");
                            continue;
                        }
                        server.privateMessage(personalMessage[1], personalMessage[2], this);
                    }
                    if (message.equals("/list")) {
                        List<String> userList = server.getUserList();
                        String joinedUsers = String.join(", ", userList);
                        sendMessage(joinedUsers);
                    }
                    if (message.startsWith("/kick") && server.getDatabase().checkUserAdmin(this.username)) {
                        String[] kickCommand = message.replaceAll("\\s+", " ").split(" ");
                        if (kickCommand.length != 2) {
                            out.writeUTF("Некорректное число аргументов");
                            continue;
                        }
                        server.kickClient(kickCommand[1], this);
                    }
                } else {
                    server.broadcastMessage(message, this);
                }
            } catch (SocketException e) {
                break;
            }
        }
    }

    public void disconnect() {
        server.removeClient(this);
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

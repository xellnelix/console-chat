package ru.otus.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
	private final Socket socket;
	private final Server server;
	private final DataInputStream in;
	private final DataOutputStream out;
	private final String username;

	public String getUsername() {
		return username;
	}

	public ClientHandler(Socket socket, Server server) throws IOException {
		this.socket = socket;
		this.server = server;
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		username = in.readUTF();
		server.addClient(this);
		new Thread(() -> {
			try {
				while (socket.isConnected()) {
					String message = in.readUTF();
					if (message.startsWith("/")) {
						if (message.equals("/exit")) {
							disconnect();
							break;
						}
					}
					if (message.startsWith("/w")) {
						String[] personalMessage =  message.replaceAll("\\s+", " ").split(" ");
						if (personalMessage.length != 3) {
							out.writeUTF("Некорректное число");
						}
						server.privateMessage(message.split(" ")[1], message.split(" ")[2]);
					} else {
						server.broadcastMessage(message, this);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				disconnect();
			}
		}).start();
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
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			disconnect();
		}
	}
}

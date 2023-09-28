package ru.otus.client;

import java.io.IOException;
import java.net.SocketException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Network network = new Network()){
            network.setCallback(new Callback() {
                @Override
                public void call(Object... args) {
                    System.out.println(args[0]);
                }
            });
            network.connect(8080);
            Scanner scanner = new Scanner(System.in);
            System.out.println("Соединение установлено");
            while (network.getSocket().isConnected()) {
                String msg = scanner.nextLine();
                try {
                    network.sendMessage(msg);
                } catch (SocketException e) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

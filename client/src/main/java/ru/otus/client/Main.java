package ru.otus.client;

import java.io.IOException;
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
            while (true) {
                String msg = scanner.nextLine();
                network.sendMessage(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

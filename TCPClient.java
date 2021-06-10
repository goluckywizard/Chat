import javax.swing.*;
import java.io.*;

import java.net.*;
import java.util.ArrayDeque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ClientsReadThread extends Thread {
    private boolean success;
    private ArrayDeque<String> otherClients;
    private ObjectInputStream fromServer;
    JTextArea chat;
    ClientsReadThread(ArrayDeque<String> otherClients, ObjectInputStream fromServer, JTextArea chat) {
        this.fromServer = fromServer;
        this.otherClients = otherClients;
        this.chat = chat;
    }

    @Override
    public void run() {
        ChatComand command;
        while (!isInterrupted()) {
            try {
                command = (ChatComand) fromServer.readObject();
                switch (command.command) {
                    case MESSAGE -> {
                        System.out.println(command.clientName+":"+command.parameter);
                        chat.append(command.clientName+":"+command.parameter+"\n");
                    }
                    case LIST -> {
                        synchronized (this) {
                            otherClients = Stream.of(command.parameter.split("\n")).collect(Collectors.toCollection(ArrayDeque::new));
                            success = true;
                            notify();
                            for (String a : otherClients) {
                                System.out.println(a);
                                chat.append(a+"\n");
                            }
                        }
                    }
                    case SUCCESS -> {
                        synchronized (this) {
                            success = true;
                            notify();
                        }
                    }
                    case ERROR -> {
                        synchronized (this) {
                            success = false;
                            notify();
                        }
                    }
                    case LOGIN -> {
                        System.out.println("Добро пожаловать!");
                    }
                }
            }
            catch (IOException | ClassNotFoundException err) {
                err.printStackTrace();
            }
        }
    }
}
/*class ClientsWrite {
    public void Run() throws IOException, ClassNotFoundException {
    }
}*/

public class TCPClient {

    public static void main(String[] args) throws IOException {
        SwingClient swingClient = new SwingClient();
        swingClient.run();

    }
}
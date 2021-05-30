import java.io.*;

import java.net.*;
import java.util.ArrayDeque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ClientsReadThread extends Thread {
    private boolean success;
    private ArrayDeque<String> otherClients;
    private ObjectInputStream fromServer;
    ClientsReadThread(ArrayDeque<String> otherClients, ObjectInputStream fromServer) {
        this.fromServer = fromServer;
        this.otherClients = otherClients;
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
                    }
                    case LIST -> {
                        synchronized (this) {
                            otherClients = Stream.of(command.parameter.split("\n")).collect(Collectors.toCollection(ArrayDeque::new));
                            success = true;
                            notify();
                            for (String a : otherClients)
                                System.out.println(a);
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
class ClientsWrite {
    public void Run() throws IOException, ClassNotFoundException {
        Socket s = new Socket("127.0.0.1", 2048);
        //OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
        ObjectOutputStream writer = new ObjectOutputStream(s.getOutputStream());

        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        //BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        ObjectInputStream reader = new ObjectInputStream(s.getInputStream());
        String line = "";
        String command = "";

        ArrayDeque<String> otherClients = new ArrayDeque<>();
        ClientsReadThread readThread = new ClientsReadThread(otherClients, reader);
        readThread.start();
        String username;
        System.out.print("Введите имя: ");
        username = consoleReader.readLine();
        ChatComand registration = new ChatComand("login", username);
        writer.writeObject(registration);
        writer.flush();
        //line = reader.readLine();
        //ChatComand fromServer = (ChatComand) reader.readObject();
        System.out.println("Получен ответ:" + line);


        do {

            System.out.print("Введите команду:");
            command = consoleReader.readLine();
            if (command.equalsIgnoreCase("exit")) break;

            ChatComand chatComand = new ChatComand(command);
            if (chatComand.command == CommandType.MESSAGE) {
                System.out.print("Введите строку:");
                line = consoleReader.readLine();
                chatComand.setParameter(line);
                //fromServer = (ChatComand) reader.readObject();

            }
            chatComand.setClientName(username);
            writer.writeObject(chatComand);
            writer.flush();
            //line = reader.readLine();
            ///fromServer = (ChatComand) reader.readObject();
            //System.out.println("Получен ответ:" + fromServer.parameter);

        } while (true);
        writer.close();
    }
}

public class TCPClient {

    public static void main(String[] args){
        SwingClient swingClient = new SwingClient();
        swingClient.run();

    }
}
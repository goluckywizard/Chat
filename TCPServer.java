import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.TreeSet;

public class TCPServer {

    public static void main(String[] args) throws IOException {
//Создаем новый серверный Socket на порту 2048
        ServerSocket s = new ServerSocket(2048);
        TreeSet<String> userlist = new TreeSet<>();
        //File chat = new File("chat.txt");
        ArrayDeque<ObjectOutputStream> usersOutput = new ArrayDeque<>();
        ArrayDeque<ChatComand> messages = new ArrayDeque<>();

        OutputProcessor outputProcessor = new OutputProcessor(usersOutput, messages);
        outputProcessor.start();
        while (true) {
            Socket clientSocket = s.accept(); //принимаем соединение
            System.out.println("Получено соединение от:" + clientSocket.getInetAddress()
                    + ":" + clientSocket.getPort());
            ObjectOutputStream fromClientObjectsOut = new ObjectOutputStream(clientSocket.getOutputStream());
            usersOutput.add(fromClientObjectsOut);
            //Создаем и запускаем поток для обработки запроса
            Thread t = new Thread(new RequestProcessor(clientSocket, userlist, messages, fromClientObjectsOut));
            System.out.println("Запуск обработчика...");
            t.start();
        }
    }
}

class OutputProcessor extends Thread {
    ArrayDeque<ObjectOutputStream> userOutput;
    ArrayDeque<ChatComand> messages;

    public OutputProcessor(ArrayDeque<ObjectOutputStream> userOutput, ArrayDeque<ChatComand> messages) {
        this.userOutput = userOutput;
        this.messages = messages;
    }
    public void addUser(ObjectOutputStream user) {
        userOutput.add(user);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            synchronized (messages) {
                try {
                    messages.wait();
                    for (ObjectOutputStream iter : userOutput) {
                        try {
                            iter.writeObject(messages.getLast());
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                            userOutput.remove(iter);
                        }
                    }
                }
                catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }
    }
}

class RequestProcessor implements Runnable {

    Socket s; //Точка установленного соединения
    TreeSet<String> userlist;
    String username;
    ArrayDeque<ChatComand> messages;
    ObjectOutputStream writer;

    public RequestProcessor(Socket s, TreeSet<String> userlist, ArrayDeque<ChatComand> messages) {
        this.s = s;
        this.userlist = userlist;
        this.messages = messages;
    }
    public RequestProcessor(Socket s, TreeSet<String> userlist, ArrayDeque<ChatComand> messages, ObjectOutputStream out) {
        this.s = s;
        this.userlist = userlist;
        this.messages = messages;
        writer = out;
    }

    RequestProcessor(Socket s) {
        this.s = s;
    }
    public void run() {
        try {
            InputStream input = s.getInputStream();
            ObjectInputStream reader = new ObjectInputStream(input);
            //OutputStream output = s.getOutputStream();
            //OutputStreamWriter writer = new OutputStreamWriter(output);
            //ObjectOutputStream writer = new ObjectOutputStream(output);
            String line = "";

            System.out.println("Ожидаем имя...");
            ChatComand comand = (ChatComand) reader.readObject();
            System.out.println("Получена строка:" + comand.parameter);
            username = comand.parameter;
            userlist.add(username);
            writer.writeObject(new ChatComand("success", "login"));
            for (ChatComand iter : messages) {
                writer.writeObject(iter);
            }
            /*writer.write("ответ от:" + s.getLocalAddress() + ":" + s.getLocalPort()
                    + " для:" + s.getInetAddress() + ":" + s.getPort() + " : " + comand.parameter + "\n");
            writer.flush();*/

            while (true) {
                System.out.println("Ожидаем команду...");
                comand = (ChatComand) reader.readObject();
                if (comand == null) break;
                if (comand.command == CommandType.MESSAGE) {
                    System.out.println(username+": "+comand.command+" "+ comand.parameter);
                    synchronized (messages) {
                        messages.add(comand);
                        writer.writeObject(new ChatComand("success", "message"));
                        messages.notify();
                    }
                }
                if (comand.command == CommandType.LIST) {
                    System.out.println(username+": "+comand.command);
                    StringBuilder parameter = new StringBuilder();
                    for (String a : userlist){
                        parameter.append(a).append("\n");
                    }
                    writer.writeObject(new ChatComand("list", parameter.toString()));
                }
                if (comand.command == CommandType.CONNECTION_CLOSE) {
                    System.out.println(username+": "+comand.command);
                    userlist.remove(username);
                    Thread.currentThread().interrupt();
                }
                /*writer.write("ответ от:" + s.getLocalAddress() + ":" + s.getLocalPort()
                        + " для:" + s.getInetAddress() + ":" + s.getPort() + " : " + comand.parameter + "\n");
                writer.flush();*/
            }

            writer.close();
            System.out.println("Обработчик завершил работу");
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}

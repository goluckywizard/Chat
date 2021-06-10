import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicReference;

public class SwingClient {
    public void run() throws IOException {
        Socket s = new Socket("127.0.0.1", 2048);
        //OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
        ObjectOutputStream writer = new ObjectOutputStream(s.getOutputStream());

        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        //BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        ObjectInputStream reader = new ObjectInputStream(s.getInputStream());
        AtomicReference<String> username = new AtomicReference<>();
        StringBuffer userBuffer;
        String line = "";
        JTextArea messages = new JTextArea();
        messages.setEditable(false);

        ArrayDeque<String> otherClients = new ArrayDeque<>();
        ClientsReadThread readThread = new ClientsReadThread(otherClients, reader, messages);
        readThread.start();

        JFrame registration = new JFrame();
        registration.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        registration.setSize(400, 400);

        JTextField forName = new JTextField("Введите имя");
        JTextField forMessage = new JTextField("qwerty");
        forMessage.setSize(100, 40);
        JButton checkUserList = new JButton();
        checkUserList.addActionListener(e -> {
            try {
                ChatComand chatComand = new ChatComand("list");
                chatComand.setClientName("username");
                writer.writeObject(chatComand);
                writer.flush();
                forMessage.setText("");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        forMessage.setToolTipText("Введите сообщение");
        forMessage.addActionListener(e -> {
            try {
                String command = "";
                command = "send";
                //if (command.equalsIgnoreCase("exit")) break;
                ChatComand chatComand = new ChatComand(command);
                chatComand.setParameter(forMessage.getText());
                chatComand.setClientName(username.get());
                writer.writeObject(chatComand);
                writer.flush();
                forMessage.setText("");
                forMessage.setSize(40, 20);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        forName.addActionListener(e -> {
            try {
                username.set(forName.getText());
                //userBuffer =
                ChatComand registration1 = new ChatComand("login", username.get());
                writer.writeObject(registration1);
                writer.flush();
                //line = reader.readLine();
                //ChatComand fromServer = (ChatComand) reader.readObject();
                //System.out.println("Получен ответ:" + line);
                registration.getContentPane().remove(forName);
                //registration.getContentPane().add(messages);
                //registration.getContentPane().add(forMessage);
                /*JPanel panel = new JPanel();*/
                Container container = registration.getContentPane();
                container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
                container.add(messages);
                container.add(forMessage);
                container.add(checkUserList);
                //registration.getContentPane().add(container);
                registration.dispose();
                registration.setVisible(true);

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        registration.getContentPane().add(forName);
        registration.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                try {
                    String command = "";
                    command = "close";
                    //if (command.equalsIgnoreCase("exit")) break;
                    ChatComand chatComand = new ChatComand(command);
                    chatComand.setParameter(forMessage.getText());
                    chatComand.setClientName(username.get());
                    writer.writeObject(chatComand);
                    writer.flush();
                    forMessage.setText("");
                    forMessage.setSize(40, 20);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        registration.setVisible(true);
    }
}

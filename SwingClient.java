import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class SwingClient {
    public void run() {
        JFrame registration = new JFrame();
        registration.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        registration.setSize(400, 400);
        JTextField forName = new JTextField("Введите имя");
        forName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        registration.getContentPane().add(forName);
        registration.setVisible(true);
    }
}

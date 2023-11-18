package chess_server_package;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Window implements ActionListener {
    private JFrame frame;
    JPanel panel;
    JButton b;
    JPanel panel1;
    JLabel label;
    public Window() {
        inicjalize();
    }

    public void inicjalize() {
        frame = new JFrame();
        this.frame.setTitle("Button");
        this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.frame.setSize(400, 300);
        this.frame.setResizable(false);
        this.frame.setLocationRelativeTo(null);

        panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.setBackground(Color.BLACK);

        b = new JButton("Button");
        b.setBackground(Color.WHITE);
        b.addActionListener(this);
        panel.add(b);


        panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel1.setBackground(Color.DARK_GRAY);

        label = new JLabel("HEY");
        label.setForeground(Color.BLACK);
        label.setFont(new Font("Dialog", Font.PLAIN, 20));
        panel1.add(label);

        frame.add(panel, BorderLayout.WEST);
        frame.add(panel1, BorderLayout.CENTER);


        this.frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == b) {
            String tmp = label.getText();
            label.setText(tmp + "!");
        }
    }
}

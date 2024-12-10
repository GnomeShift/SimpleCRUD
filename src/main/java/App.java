import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class App {
    private JFrame mainFrame;
    private JLabel helloLabel;
    private JButton addDataButton;
    private JButton getDataButton;
    private JButton deleteDataButton;
    private JPanel panel;
    private JPanel controlPanel;

    public App() {
        createMain();
    }

    public void createMain() {
        mainFrame = new JFrame("SimpleCRUD");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(400, 300);
        mainFrame.setLayout(new BorderLayout());

        panel = new JPanel(new BorderLayout());
        mainFrame.add(panel, BorderLayout.CENTER);

        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        helloLabel = new JLabel("Выберите действие:");
        controlPanel.add(helloLabel);
        controlPanel.add(Box.createVerticalStrut(10));

        addDataButton = new JButton("Добавить данные");
        addDataButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new DataManage.Create();
                mainFrame.dispose();
            }
        });
        controlPanel.add(addDataButton);

        getDataButton = new JButton("Получить данные");
        getDataButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        getDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new DataManage.Read().showData();
                mainFrame.dispose();
                helloLabel.setVisible(false);
                mainFrame.revalidate();
                mainFrame.repaint();
            }
        });
        controlPanel.add(getDataButton);

        deleteDataButton = new JButton("Удалить данные");
        deleteDataButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new DataManage.Delete().showData();
                mainFrame.dispose();
           }
        });
        controlPanel.add(deleteDataButton);

        mainFrame.add(controlPanel, BorderLayout.NORTH);
        mainFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new App();
            }
        });
    }
}

package palezhaa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import static palezhaa.Client.getData;

public class SecondPage extends JPanel {

    private MainFrame parent;
    private JLabel label;
    private JButton back;
    private String header[] = {"Name", "Surname", "age"};
    private JTable table;
    private JScrollPane scrollPane;


    public SecondPage(MainFrame parent) {

        this.parent = parent;

        setSize(500, 500);
        setLayout(null);

        label = new JLabel("All students");
        label.setSize(300, 30);
        label.setLocation(100, 100);
        add(label);

        back = new JButton("Back");
        back.setSize(300, 30);
        back.setLocation(100, 150);
        add(back);
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                parent.getSecondPage().setVisible(false);
                parent.getMainMenuPage().setVisible(true);
            }
        });

        table = new JTable();
        table.setFont(new Font("Calibri", Font.PLAIN, 30));
        table.setRowHeight(30);

        scrollPane = new JScrollPane(table);
        scrollPane.setSize(300, 200);
        scrollPane.setLocation(100, 200);
        add(scrollPane);

    }

    public void generateTable(ArrayList<Students> student) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"name", "surname", "age"}, 0);
        for (Students students : student) {
            model.addRow(new Object[]{student});
        }
        table.setModel(model);
    }


}




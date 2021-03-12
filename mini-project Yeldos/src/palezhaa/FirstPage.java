package palezhaa;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static palezhaa.Client.setData;
import palezhaa.Students;
import palezhaa.PackageData;
import palezhaa.Client;


public class FirstPage extends JPanel {

    private MainFrame parent;
    private DaoImpl Daoparent;
    private JLabel label;
    private JButton back;
    private JButton button;
    private JTextField nameField;
    private JTextField surnameField;
    private JTextField ageField;
    private JTextArea area;
    private String age;


    public FirstPage(MainFrame parent) {

        this.parent = parent;

        setSize(500,500);
        setLayout(null);

        label = new JLabel("Name:");
        label.setSize(300,30);
        label.setLocation(100,50);
        add(label);

        nameField = new JTextField();
        nameField.setSize(200,30);
        nameField.setLocation(200,50);
        add(nameField);

        label = new JLabel("Surname:");
        label.setSize(300,30);
        label.setLocation(100,100);
        add(label);

        surnameField = new JTextField();
        surnameField.setSize(200,30);
        surnameField.setLocation(200,100);
        add(surnameField);

        label = new JLabel("age:");
        label.setSize(300,30);
        label.setLocation(100,150);
        add(label);
        ageField = new JTextField();
        ageField.setSize(200,30);
        ageField.setLocation(200,150);
        add(ageField);

        back = new JButton("Back");
        back.setSize(150,30);
        back.setLocation(300,400);
        add(back);
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                parent.getFirstPage().setVisible(false);
                parent.getMainMenuPage().setVisible(true);
            }
        });
        button = new JButton("ADD");
        button.setSize(150,30);
        button.setLocation(100,400);

        button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                String surname = surnameField.getText();
                String name = nameField.getText();
               // int age = Integer.parseInt(ageField.getText());
                String age = ageField.getText();
                if (!(surname == "") && !(name == "") && !(age == "")){
                    Students student = new Students(name, surname,Integer.parseInt(age) );
                    nameField.setText("");
                    surnameField.setText("");
                    ageField.setText("");
                    setData(Client.sockets, new PackageData( "add",null, student));
                }
            }
        });
        add(button);
    }


}

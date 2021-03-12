import models.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

//Приложение Администратор
public class Admin extends JFrame implements ActionListener, TableModelListener {

    private JPanel adminPanel;
    private JTable taircrafts; //таблица самолетов
    private JTable tcities; //таблица гордов
    private JTable tflights; //таблица рейсов
    private JButton save;
    private JButton adda;
    private JButton dela;
    private JButton addc;
    private JButton delc;
    private JButton addf;
    private JButton delf;
    private JLabel lm;
    private String hostname; //URL адрес сервера
    private int port; //порт
    private List<Aircrafts> la = null; //модель приложения самолетов
    private List<Cities> lc = null; //модель приложения городов
    private List<Flights> lf = null; //модель приложения рейсов

    //Соединяется с сервером передавая либо объект либо строку. Возвращает ответ сервера в виде объекта либо строки
    public Object GetFromServer(Object query, boolean sIsObject, boolean rIsObject) {
        Object result = null;
        try (Socket socket = new Socket(hostname, port)) {
            OutputStream output = socket.getOutputStream();
            if (sIsObject) {
                ObjectOutputStream objo = new ObjectOutputStream(output);
                objo.writeObject(query);
            } else {
                PrintWriter writer = new PrintWriter(output, true);
                writer.println(query);
            }
            if (rIsObject) {
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                result = input.readObject();
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                result = reader.readLine();
            }
        } catch (IOException | ClassNotFoundException ex) {
        }
        return result;
    }


    public Admin() {

        //Чтение файла конфигурации
        try {
            String bp = new File(".").getCanonicalPath();
            if (bp.charAt(bp.length() - 1) != '\\') bp += '\\';
            bp += "config.txt";
            BufferedReader br = new BufferedReader(new FileReader(bp));
            hostname = br.readLine();
            port = Integer.valueOf(br.readLine());
            br.close();
        } catch (IOException e) {
            setTitle("Ошибка с файлом конфигурации.");
            return;
        }

        setContentPane(adminPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setSize(800, 600);
        setTitle("Администратор");
        setVisible(true);

        //Выводим список самолетов
        DefaultTableModel dtm = new javax.swing.table.DefaultTableModel(
                new Object[]{"Ид", "Название", "Модель", "Эконом класс, мест", "Бизнес класс, мест"}, 0);
        taircrafts.setModel(dtm);
        Object obj = GetFromServer(new TcpData("0", "SELECT * FROM aircrafts"), true, true);
        if (obj == null) {
            lm.setText("Ошибка. Не далось подключиться к серверу.");
            return;
        }
        la = (List<Aircrafts>) ((TcpData) obj).data;
        for (Aircrafts a : la) {
            dtm.addRow(new Object[]{a.getId(), a.getName(), a.getModel(), a.getEconom_class_capacity(), a.getBusiness_class_capacity()});
        }
        dtm.addTableModelListener(this);
        taircrafts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //Выводим список городов
        dtm = new DefaultTableModel(new Object[]{"Ид", "Название", "Страна", "Короткое имя города"}, 0);
        tcities.setModel(dtm);
        lc = (List<Cities>) ((TcpData) GetFromServer(new TcpData("1", "SELECT * FROM cities"), true, true)).data;
        for (Cities a : lc) {
            dtm.addRow(new Object[]{a.getId(), a.getName(), a.getCountry(), a.getShort_name()});
        }
        dtm.addTableModelListener(this);
        tcities.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //Выводим список рейсов
        dtm = new DefaultTableModel(new Object[]{"Ид", "Ид самолета", "Отправление, ид. города", "Прибытие, ид. города", "Время полета, час.", "Ц. эк. кл., руб.", "Ц.б.кл, руб."}, 0);
        tflights.setModel(dtm);
        lf = (List<Flights>) ((TcpData) GetFromServer(new TcpData("2", "SELECT * FROM flights"), true, true)).data;
        for (Flights a : lf) {
            dtm.addRow(new Object[]{a.getId(), a.getAircraft_id(), a.getDeparture_city_id(), a.getArrival_city_id(),
                    a.getDeparture_time(), a.getEconom_place_price(), a.getBusiness_place_price()});
        }
        dtm.addTableModelListener(this);
        tflights.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //Обновляет базу данных
        save.addActionListener(this);

        //Добавляют и удаляют записи в таблицах базы данных
        adda.addActionListener(this);
        dela.addActionListener(this);
        addc.addActionListener(this);
        delc.addActionListener(this);
        addf.addActionListener(this);
        delf.addActionListener(this);
    }

    public static void main(String[] args) {
        Admin admin = new Admin();
    }

    //Обновляет базу данных
    @Override
    public void actionPerformed(ActionEvent e) {

        //Обновление базы данных
        if (e.getSource() == save) {
            List<Object> udata = new ArrayList<>();
            udata.add(la);
            udata.add(lc);
            udata.add(lf);
            GetFromServer(new TcpData("4", udata), true, true);
        }

        //Добавление новой строки в таблицу самолетов
        if (e.getSource() == adda) {
            Aircrafts a = new Aircrafts(0, " ", " ", 0, 0);
            la.add(a);
            DefaultTableModel dtm = (DefaultTableModel) taircrafts.getModel();
            dtm.addRow(new Object[]{a.getId(), a.getName(), a.getModel(), a.getBusiness_class_capacity(), a.getEconom_class_capacity()});
        }

        //Удаление строки из таблицы самолетов
        if (e.getSource() == dela) {
            int sr = taircrafts.getSelectedRow();
            if (sr != -1) {
                DefaultTableModel dtm = new javax.swing.table.DefaultTableModel(
                        new Object[]{"Ид", "Название", "Модель", "Эконом класс, мест", "Бизнес класс, мест"}, 0);
                for (int i = 0; i < la.size(); i++) {
                    if (i != sr)
                        dtm.addRow(new Object[]{la.get(i).getId(), la.get(i).getName(), la.get(i).getModel(),
                                la.get(i).getEconom_class_capacity(), la.get(i).getBusiness_class_capacity()});
                }
                dtm.addTableModelListener(this);
                taircrafts.setModel(dtm);
                la.remove(sr);
            }
        }

        //Добавление новой строки в таблицу городов
        if (e.getSource() == addc) {
            Cities a = new Cities(0, " ", " ", " ");
            lc.add(a);
            DefaultTableModel dtm = (DefaultTableModel) tcities.getModel();
            dtm.addRow(new Object[]{a.getId(), a.getName(), a.getCountry(), a.getShort_name()});
        }

        //Удаление строки из таблицы городов
        if (e.getSource() == delc) {
            int sr = tcities.getSelectedRow();
            if (sr != -1) {
                DefaultTableModel dtm = new javax.swing.table.DefaultTableModel(
                        new Object[]{"Ид", "Название", "Страна", "Короткое имя города"}, 0);
                for (int i = 0; i < lc.size(); i++) {
                    if (i != sr)
                        dtm.addRow(new Object[]{lc.get(i).getId(), lc.get(i).getName(), lc.get(i).getCountry(), lc.get(i).getShort_name()});
                }
                dtm.addTableModelListener(this);
                tcities.setModel(dtm);
                lc.remove(sr);
            }
        }

        //Добавление новой строки в таблицу рейсов
        if (e.getSource() == addf) {
            Flights a = new Flights(0, 0, 0, 0, " ", 0, 0);
            lf.add(a);
            DefaultTableModel dtm = (DefaultTableModel) tflights.getModel();
            dtm.addRow(new Object[]{a.getId(), a.getAircraft_id(), a.getDeparture_city_id(), a.getArrival_city_id(), a.getDeparture_time(),
                    a.getEconom_place_price(), a.getBusiness_place_price()});
        }

        //Удаление строки из таблицы рейсов
        if (e.getSource() == delf) {
            int sr = tflights.getSelectedRow();
            if (sr != -1) {
                DefaultTableModel dtm = new javax.swing.table.DefaultTableModel(
                        new Object[]{"Ид", "Ид самолета", "Отправление, ид. города", "Прибытие, ид. города", "Время полета, час.", "Ц. эк. кл., руб.", "Ц.б.кл, руб."}, 0);
                for (int i = 0; i < lf.size(); i++) {
                    if (i != sr)
                        dtm.addRow(new Object[]{lf.get(i).getId(), lf.get(i).getAircraft_id(), lf.get(i).getDeparture_city_id(), lf.get(i).getArrival_city_id(), lf.get(i).getDeparture_time(),
                                lf.get(i).getEconom_place_price(), lf.get(i).getBusiness_place_price()});
                }
                dtm.addTableModelListener(this);
                tflights.setModel(dtm);
                lf.remove(sr);
            }
        }
    }

    //Обновляет модель приложения при изменении данных в таблицах
    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getSource() == taircrafts.getModel()) {
            TableModel tm = taircrafts.getModel();
            Aircrafts a = new Aircrafts(Integer.parseInt(tm.getValueAt(e.getFirstRow(), 0).toString()),
                    tm.getValueAt(e.getFirstRow(), 1).toString(),
                    tm.getValueAt(e.getFirstRow(), 2).toString(),
                    Integer.parseInt(tm.getValueAt(e.getFirstRow(), 4).toString()),
                    Integer.parseInt(tm.getValueAt(e.getFirstRow(), 3).toString())
            );
            la.set(e.getFirstRow(), a);
        }
        if (e.getSource() == tcities.getModel()) {
            TableModel tm = tcities.getModel();
            Cities c = new Cities(Integer.parseInt(tm.getValueAt(e.getFirstRow(), 0).toString()),
                    tm.getValueAt(e.getFirstRow(), 1).toString(),
                    tm.getValueAt(e.getFirstRow(), 2).toString(),
                    tm.getValueAt(e.getFirstRow(), 3).toString());
            lc.set(e.getFirstRow(), c);
        }
        if (e.getSource() == tflights.getModel()) {
            TableModel tm = tflights.getModel();
            Flights f = new Flights(Integer.parseInt(tm.getValueAt(e.getFirstRow(), 0).toString()),
                    Integer.parseInt(tm.getValueAt(e.getFirstRow(), 1).toString()),
                    Integer.parseInt(tm.getValueAt(e.getFirstRow(), 2).toString()),
                    Integer.parseInt(tm.getValueAt(e.getFirstRow(), 3).toString()),
                    tm.getValueAt(e.getFirstRow(), 4).toString(),
                    Integer.parseInt(tm.getValueAt(e.getFirstRow(), 5).toString()),
                    Integer.parseInt(tm.getValueAt(e.getFirstRow(), 6).toString())
            );
            lf.set(e.getFirstRow(), f);
        }
    }
}

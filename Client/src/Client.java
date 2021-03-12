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

//Приложение Кассир
public class Client extends JFrame implements ActionListener, TableModelListener {

    private JPanel clientPanel;
    private JTable taircrafts; //таблица самолетов
    private JTable tcities; //таблица городов
    private JTable tflights; //таблица рейсов
    private JTable ttickets; //таблица билетов
    private JButton save;
    private JButton addt;
    private JButton delt;
    private JLabel lm; //метка для отображения сообщения пользователю
    private String hostname; //URL адрес сервера
    private int port; //порт
    private List<Aircrafts> la = null; //модель приложения самолетов
    private List<Cities> lc = null; //модель приложения городов
    private List<Flights> lf = null; //модель приложения рейсов
    private List<Tickets> lt = null; //модель приложения билетов

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

    //Конструктор приложения
    public Client() {

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

        setContentPane(clientPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setSize(800, 600);
        setTitle("Кассир");
        setVisible(true);

        //Выводим список самолетов
        DefaultTableModel dtm = new javax.swing.table.DefaultTableModel(
                new Object[]{"Ид", "Название", "Модель", "Эконом класс, мест", "Бизнес класс, мест"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
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
        dtm = new DefaultTableModel(new Object[]{"Ид", "Название", "Страна", "Короткое имя города"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tcities.setModel(dtm);
        lc = (List<Cities>) ((TcpData) GetFromServer(new TcpData("1", "SELECT * FROM cities"), true, true)).data;
        for (Cities a : lc) {
            dtm.addRow(new Object[]{a.getId(), a.getName(), a.getCountry(), a.getShort_name()});
        }
        dtm.addTableModelListener(this);
        tcities.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //Выводим список рейсов
        dtm = new DefaultTableModel(new Object[]{"Ид", "Ид самолета", "Отправление, ид. города", "Прибытие, ид. города", "Время полета, час.",
                "Ц. эк. кл., руб.", "Ц.б.кл, руб."}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tflights.setModel(dtm);
        lf = (List<Flights>) ((TcpData) GetFromServer(new TcpData("2", "SELECT * FROM flights"), true, true)).data;
        for (Flights a : lf) {
            dtm.addRow(new Object[]{a.getId(), a.getAircraft_id(), a.getDeparture_city_id(), a.getArrival_city_id(),
                    a.getDeparture_time(), a.getEconom_place_price(), a.getBusiness_place_price()});
        }
        dtm.addTableModelListener(this);
        tflights.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //Выводим список билетов
        dtm = new DefaultTableModel(new Object[]{"Ид", "Ид рейса", "Имя", "Фамилия", "Номер паспорта", "Тип билета"}, 0);
        ttickets.setModel(dtm);
        lt = (List<Tickets>) ((TcpData) GetFromServer(new TcpData("3", "SELECT * FROM tickets"), true, true)).data;
        for (Tickets a : lt) {
            dtm.addRow(new Object[]{a.getId(), a.getFlight_id(), a.getName(),
                    a.getSurname(), a.getPassport_number(), a.getTicket_type()});
        }
        dtm.addTableModelListener(this);
        ttickets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //Обновляет базу данных
        save.addActionListener(this);

        //Добавляют и удаляют записи в таблицах базы данных
        addt.addActionListener(this);
        delt.addActionListener(this);
    }

    //Главная функция приложения
    public static void main(String[] args) {
        Client client = new Client();
    }

    //Обновляет базу данных
    @Override
    public void actionPerformed(ActionEvent e) {

        //Валидация вводимых кассиром данных
        //Нажата кнопки сохранения данных
        if (e.getSource() == save) {

            //При сохранении данных проверяем не имеются ли пустые не заполненные строки и есть
            //есть удаляем их
            List<Tickets> nlt = new ArrayList<>(lt.size());
            DefaultTableModel dtm = (DefaultTableModel) ttickets.getModel();
            dtm.removeTableModelListener(this);
            dtm.setRowCount(0);
            for (int i = 0; i < lt.size(); i++) {
                if (lt.get(i).getId() != 0 &&
                        lt.get(i).getFlight_id() != 0 &&
                        !lt.get(i).getName().trim().isEmpty() &&
                        !lt.get(i).getSurname().trim().isEmpty() &&
                        !lt.get(i).getPassport_number().trim().isEmpty() &&
                        (lt.get(i).getTicket_type().equals("ec") || lt.get(i).getTicket_type().equals("bc"))) {
                    nlt.add(lt.get(i));
                    dtm.addRow(new Object[]{lt.get(i).getId(), lt.get(i).getFlight_id(), lt.get(i).getName(),
                            lt.get(i).getSurname(), lt.get(i).getPassport_number(), lt.get(i).getTicket_type()});
                }
            }
            lt = nlt;
            dtm.addTableModelListener(this);

            //Обновляем базу данных
            List<Object> udata = new ArrayList<>();
            udata.add(lt);
            GetFromServer(new TcpData("5", udata), true, true);
        }

        //Обработчики нажатия кнопок добавления новой записи и удаления записей для таблицы билетов
        if (e.getSource() == addt) {
            Tickets a = new Tickets(0, 0, " ", " ", " ", "bc");
            lt.add(a);
            DefaultTableModel dtm = (DefaultTableModel) ttickets.getModel();
            dtm.removeTableModelListener(this);
            dtm.addRow(new Object[]{a.getId(), a.getFlight_id(), a.getName(), a.getSurname(), a.getPassport_number(), a.getTicket_type()});
            dtm.addTableModelListener(this);
        }
        if (e.getSource() == delt) {
            int sr = ttickets.getSelectedRow();
            if (sr != -1) {
                ttickets.getModel().removeTableModelListener(this);
                DefaultTableModel dtm = new javax.swing.table.DefaultTableModel(

                        new Object[]{"Ид", "Ид рейса", "Имя", "Фамилия", "Номер паспорта", "Тип билета"}, 0);
                for (int i = 0; i < lt.size(); i++) {
                    if (i != sr)
                        dtm.addRow(new Object[]{lt.get(i).getId(), lt.get(i).getFlight_id(), lt.get(i).getName(), lt.get(i).getSurname(),
                                lt.get(i).getPassport_number(), lt.get(i).getTicket_type()});
                }
                dtm.addTableModelListener(this);
                ttickets.setModel(dtm);
                lt.remove(sr);
            }
        }
    }


    //Обновляет модель приложения при изменении данных в таблицах
    @Override
    public void tableChanged(TableModelEvent e) {
        TableModel tm = ttickets.getModel();

        //Валидация вводимых кассиром данных
        //Проверка уникальности первичного ключа
        if (e.getColumn() == 0) {
            boolean exist = false;
            try {
                for (int i = 0; i < lf.size(); i++) {
                    if (lf.get(i).getId() == Integer.parseInt(tm.getValueAt(e.getFirstRow(), e.getColumn()).toString())) {
                        exist = true;
                        break;
                    }
                }
            } catch (NumberFormatException ex) {
                exist = true;
            }
            if (exist) {
                lm.setText("Ошибка. Данный идентификатор билета уже есть в базе данных либо имеет неверный формат.");
                tm.removeTableModelListener(this);
                tm.setValueAt(lt.get(e.getFirstRow()).getId(), e.getFirstRow(), e.getColumn());
                tm.addTableModelListener(this);
                return;
            } else lm.setText("");
        }

        //Проверка существования кода рейса
        if (e.getColumn() == 1) {
            boolean exist = false;
            try {
                for (int i = 0; i < lf.size(); i++) {
                    if (lf.get(i).getId() == Integer.parseInt(tm.getValueAt(e.getFirstRow(), e.getColumn()).toString())) {
                        exist = true;
                        break;
                    }
                }
            } catch (NumberFormatException ex) {
            }
            if (!exist) {
                lm.setText("Ошибка. Данного рейса нет в списке рейсов.");
                tm.removeTableModelListener(this);
                tm.setValueAt(lt.get(e.getFirstRow()).getFlight_id(), e.getFirstRow(), e.getColumn());
                tm.addTableModelListener(this);
                return;
            } else lm.setText("");
        }

        //проверка корректности типа билета
        if (e.getColumn() == 5) {
            tm.removeTableModelListener(this);
            String val = tm.getValueAt(e.getFirstRow(), e.getColumn()).toString().toLowerCase();
            if (!val.equals("ec") && !val.equals("bc")) {
                lm.setText("Ошибка. Данный тип билета не допустим.");
                tm.setValueAt(lt.get(e.getFirstRow()).getTicket_type(), e.getFirstRow(), e.getColumn());
                return;
            } else {
                tm.setValueAt(val, e.getFirstRow(), e.getColumn());
                lm.setText("");
            }
            tm.addTableModelListener(this);
        }

        //обновление модели приложения
        Tickets t = new Tickets(Integer.parseInt(tm.getValueAt(e.getFirstRow(), 0).toString()),
                Integer.parseInt(tm.getValueAt(e.getFirstRow(), 1).toString()),
                tm.getValueAt(e.getFirstRow(), 2).toString(),
                tm.getValueAt(e.getFirstRow(), 3).toString(),
                tm.getValueAt(e.getFirstRow(), 4).toString(),
                tm.getValueAt(e.getFirstRow(), 5).toString()
        );
        lt.set(e.getFirstRow(), t);
    }
}

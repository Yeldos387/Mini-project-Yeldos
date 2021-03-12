import models.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//Приложение Cервер
public class Main extends JFrame {

    //Класс запускающий сервер в отдельном потоке
    class MainServer extends Thread {
        private ServerSocket serverSocket;
        private int port;

        //Остановка сервера по требованию
        public void setStop() {
            try {
                serverSocket.close();
            } catch (IOException ex) {
            }
        }

        @Override
        public void run() {
            try {
                System.out.println("Сервер запущен и прослушивает порт: " + port);
                while (true) {

                    //Ждем соединения и смотрим какой код запроса прислал клиент
                    Socket socket = serverSocket.accept();
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    TcpData td = (TcpData) ois.readObject();
                    System.out.println("Запрос клиента, комманда: " + td.cmd);
                    Object result = null;

                    //Соединяемся с базой данных
                    try (Connection conn = DriverManager.getConnection(cs)) {
                        Statement s = conn.createStatement();
                        ResultSet rs = null;
                        if (!td.cmd.equals("4") && !td.cmd.equals("5")) rs = s.executeQuery((String) td.data);
                        switch (td.cmd) {
                            case "0": //Получить список самолетов
                                List<Aircrafts> alist = new ArrayList<>(25);
                                while (rs.next()) {
                                    Aircrafts ac = new Aircrafts(
                                            rs.getInt("id"),
                                            rs.getString("name"),
                                            rs.getString("model"),
                                            rs.getInt("business_class_capacity"),
                                            rs.getInt("econom_class_capacity")
                                    );
                                    alist.add(ac);
                                }
                                result = alist;
                                break;

                            case "1": //Получить список городов
                                List<Cities> cities = new ArrayList<>(25);
                                while (rs.next()) {
                                    Cities c = new Cities(
                                            rs.getInt("id"),
                                            rs.getString("name"),
                                            rs.getString("country"),
                                            rs.getString("short_name")
                                    );
                                    cities.add(c);
                                }
                                result = cities;
                                break;

                            case "2": //Получить список рейсов
                                List<Flights> flights = new ArrayList<>(25);
                                while (rs.next()) {
                                    Flights c = new Flights(
                                            rs.getInt("id"),
                                            rs.getInt("aircraft_id"),
                                            rs.getInt("departure_city_id"),
                                            rs.getInt("arrival_city_id"),
                                            rs.getString("departure_time"),
                                            rs.getInt("econom_place_price"),
                                            rs.getInt("business_place_price")
                                    );
                                    flights.add(c);
                                }
                                result = flights;
                                break;

                            case "3": //Получить список билетов
                                List<Tickets> tickets = new ArrayList<>(25);
                                while (rs.next()) {
                                    Tickets c = new Tickets(
                                            rs.getInt("id"),
                                            rs.getInt("flight_id"),
                                            rs.getString("name"),
                                            rs.getString("surname"),
                                            rs.getString("passport_number"),
                                            rs.getString("ticket_type")
                                    );
                                    tickets.add(c);
                                }
                                result = tickets;
                                break;


                            case "4": //Обновить списки самолетов, городов, рейсов
                                //Получаем пришедшие данные
                                List<Object> udata = (List<Object>) td.data;
                                List<Aircrafts> la = (List<Aircrafts>) udata.get(0);
                                List<Cities> lc = (List<Cities>) udata.get(1);
                                List<Flights> lf = (List<Flights>) udata.get(2);

                                //Для каждой таблицы базы данных обновляем записи
                                String ds = "DELETE FROM aircrafts";
                                s.executeUpdate(ds);
                                for (Aircrafts a : la) {
                                    s.executeUpdate("INSERT INTO aircrafts (id,name,model,business_class_capacity,econom_class_capacity)" +
                                            " VALUES(" + a.getId() + ",'" + a.getName() + "','" + a.getModel() + "'," +
                                            a.getBusiness_class_capacity() + "," + a.getEconom_class_capacity() + ")");
                                }

                                ds = "DELETE FROM cities";
                                s.executeUpdate(ds);
                                for (Cities a : lc) {
                                    s.executeUpdate("INSERT INTO cities (id,name,country,short_name) VALUES("
                                            + a.getId() + ",'" + a.getName() + "','" + a.getCountry() + "','" + a.getShort_name() + "')");
                                }

                                ds = "DELETE FROM flights";
                                s.executeUpdate(ds);
                                for (Flights a : lf) {
                                    s.executeUpdate("INSERT INTO flights (id,aircraft_id,departure_city_id,arrival_city_id,departure_time," +
                                            "econom_place_price,business_place_price) VALUES(" + a.getId() + "," + a.getAircraft_id() + "," + a.getDeparture_city_id() + "," +
                                            a.getArrival_city_id() + ",'" + a.getDeparture_time() + "'," + a.getEconom_place_price() + "," + a.getBusiness_place_price() + ")");
                                }
                                break;

                            case "5": //Обновить список билетов
                                udata = (List<Object>) td.data;
                                List<Tickets> lt = (List<Tickets>) udata.get(0);

                                ds = "DELETE FROM tickets";
                                s.executeUpdate(ds);
                                for (Tickets a : lt) {
                                    s.executeUpdate("INSERT INTO tickets (id,flight_id,name,surname,passport_number,ticket_type) VALUES("
                                            + a.getId() + "," + a.getFlight_id() + ",'" + a.getName() + "','" +
                                            a.getSurname() + "','" + a.getPassport_number() + "','" + a.getTicket_type() + "')");
                                }
                                break;
                        }
                    } catch (SQLException throwables) {
                    }

                    //Возвращаем ответ клиенту
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    output.writeObject(new TcpData("OK", result));

                    //Закрываем соединение с базой данных
                    try {
                        socket.close();
                    } catch (Exception ex) {
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
            }
        }

        public MainServer(int port) {
            try {
                serverSocket = new ServerSocket(port);
                this.port = port;
            } catch (IOException ex) {
            }
        }
    }


    private JPanel mainPanel;
    private JButton start;
    private JButton stop;
    private boolean stopServer = false;
    private MainServer ms;
    private int port; //порт
    private String cs; //строка соединения с базой данных. Необходимо указать свой путь на сервере к файлу базы данных


    public Main() {

        //Чтение файла конфигурации
        try {
            String bp = new File(".").getCanonicalPath();
            if (bp.charAt(bp.length() - 1) != '\\') bp += '\\';
            bp += "config.txt";
            BufferedReader br = new BufferedReader(new FileReader(bp));
            cs = br.readLine();
            port = Integer.valueOf(br.readLine());
            br.close();
        } catch (IOException e) {
            setTitle("Ошибка с файлом конфигурации.");
            return;
        }

        setTitle("Сервер");
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        start.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (ms != null) {
                    ms.setStop();
                }
                ms = new MainServer(port); //Запускает сервер
                ms.start();
            }
        });

        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ms != null) {
                    ms.setStop(); //Останавливает сервер
                    System.out.println("Сервер остановлен");
                }
            }
        });

    }

    public static void main(String[] args) {
        Main main = new Main();
    }
}

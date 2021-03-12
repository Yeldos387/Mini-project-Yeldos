package palezhaa;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    public static Sockets sockets;
    public static void main(String[] args) {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);

        Students students = new Students("newproject", "NewSurname", 9889);
        DAO dao = new DaoImpl();
        dao.addStident(students);

        Scanner in = new Scanner(System.in);
        try {
            Socket socket = new Socket("127.0.0.1", 9889);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            PackageData data;
            sockets = new Sockets(socket, outputStream, inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setData(Sockets sockets, PackageData data){
        try {
            sockets.outputStream.writeObject(data);
            sockets.outputStream.flush();
            sockets.outputStream.reset();
        } catch ( IOException e) {
            e.printStackTrace();
        }
    }
    public static PackageData getData(Sockets sockets) {
        PackageData dataFromServer;
        try {
            sockets.outputStream.writeObject(new PackageData("list", null,null));
            sockets.outputStream.flush();
            sockets.outputStream.reset();
            while (true) {
                if ((dataFromServer = (PackageData) sockets.inputStream.readObject()) != null) {
                    return dataFromServer;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Students[] getArrayOf(ArrayList<Students> students) {
        Students[] studentsArray = new Students[students.size()];
        int i = 0;
        for (Students s : students) {
            studentsArray[i] = s;
            i++;
        }
        return studentsArray;
    }

}


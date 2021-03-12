package palezhaa;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler extends Thread {
    private Socket socket;
    private int id;

    public ClientHandler(Socket socket, int id) {
        this.socket = socket;
        this.id = id;
    }

    public void run() {
        System.out.println("CH is running");
        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            PackageData data = null;
            PackageData response = null;
            DAO dao = new DaoImpl();

            while ((data = (PackageData) inputStream.readObject()) != null) {
                if (data.getOperationType().equalsIgnoreCase("list")) {
                    System.out.println("list");
                    response = new PackageData("list", dao.getStudensts(), null);
                } else if (data.getOperationType().equalsIgnoreCase("add")) {
                    outputStream.writeObject(response);
                    outputStream.reset();
                    dao.addStident(data.getStudent());
                    outputStream.flush();
                }
            }
        } catch (Exception e) {
            try {
                System.out.println(socket.getKeepAlive());
            } catch (SocketException socketException) {
                socketException.printStackTrace();
            }
        }
    }
}

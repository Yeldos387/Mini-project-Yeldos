package palezhaa;
import java.sql.Connection;
import java.sql.DriverManager;

public class DbManager {



    public Connection connect(){
        Connection connection = null;
        try{
            //Подтягиваем драйвер, который мы добавили в самом начале урока
            Class.forName("com.mysql.cj.jdbc.Driver");

            //Создаем подключение.
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/miniproject?useUnicode=true&serverTimezone=UTC","root", "");
            System.out.println("CONNECTED");
        }catch (Exception e){
            e.printStackTrace();
        }
        return connection;
    }
}

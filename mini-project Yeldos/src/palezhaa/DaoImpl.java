package palezhaa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DaoImpl implements DAO {

    DbManager manager = new DbManager();

    @Override
    public Students getStudentByName(String name) {
        String sql = "select * from students where name = ?";
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;
        Students student = null;
        try {
            connection = manager.connect();
            statement = connection.prepareStatement(sql);
            statement.setString(1,name);
            set = statement.executeQuery();
            while (set.next()){
                Long id = set.getLong("id");
                String nameof = set.getString("name");
                String surname = set.getString("surname");
                int age = set.getInt("age");
                student = new Students(id, nameof, surname,age);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                if (set != null) {
                    set.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return student;
    }

    @Override
    public ArrayList<Students> getStudensts() {
        String sql = "select * from students";
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;
        ArrayList<Students> students = new ArrayList<>();
        connection = manager.connect();
        try {
            statement = connection.prepareStatement(sql);
            set = statement.executeQuery();
            while (set.next()){
                Long id = set.getLong("id");
                String nameof = set.getString("name");
                String surname = set.getString("surname");
                int age = set.getInt("age");
                Students student = new Students(id, nameof, surname,age);
                students.add(student);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (set != null) {
                    set.close();
                }
                if (statement != null) {
                    statement.close();
                }
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return students;
    }

    @Override
    public void addStident(Students student) {
        String sql = "insert into students(name, surname, age) values(?,?,?)";
        Connection connection = null;
        PreparedStatement statement = null;
        connection = manager.connect();
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, student.getName());
            statement.setString(2, student.getSurname());
            statement.setInt(3, student.getAge());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

package palezhaa;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class Students implements Serializable {

     String name;
     String surname;
     int age;

    public Students(String newName, String nesSurname, int i) {
        this.name = newName;
        this.surname= nesSurname;
        this.age = i;
    }

    public Students(int id, String name, String surname, int age) {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Students(Long id, String name, String surname, int age) {
        this.name = name;
        this.surname = surname;
        this.age = age;
    }

    @Override
    public String toString() {
        return "Students{" +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", age=" + age +
                '}';
    }


}

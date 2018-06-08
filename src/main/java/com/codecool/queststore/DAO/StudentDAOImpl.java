package com.codecool.queststore.DAO;

import com.codecool.queststore.model.Transaction;
import com.codecool.queststore.model.classRoom.ClassRoom;
import com.codecool.queststore.model.inventory.Category;
import com.codecool.queststore.model.inventory.Inventory;
import com.codecool.queststore.model.inventory.Item;
import com.codecool.queststore.model.user.Student;
import com.codecool.queststore.model.user.UserDetails;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDAOImpl implements StudentDAO {

    private DAOFactory factory;

    public StudentDAOImpl(DAOFactory factory) {
        this.factory = factory;
    }

    public Integer add(Student student) {
        String query = "INSERT INTO student (codecooler_id, class_id) VALUES (?, ?);";
        int id = factory.getUserDAO().add(student.getUserDetails());
        factory.execQueryInt(query, id, student.getClassRoom().getId());
        return id;
    }

    public void remove(int id) {
        factory.getUserDAO().remove(id);
    }

    public void update(Student student) {
        String query = "UPDATE student SET class_id = ? WHERE student_id = ?";
        try {
            PreparedStatement ps = factory.getConnection().prepareStatement(query);
            ps.setInt(1, student.getClassRoom().getId());
            ps.setInt(2, student.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getSQLState());
        }
        factory.getUserDAO().update(student.getUserDetails());
    }

    public Student getStudent(int id) {
        Student result = null;
        String query = "SELECT * FROM student INNER JOIN codecooler ON student.student_id = codecooler.codecooler_id WHERE student.student_id = ? LIMIT 1";
        ResultSet rs = factory.execQuery(query, id);
        try {
            rs.next();
            UserDetails ud = factory.getUserDAO().getUser(id);
            ClassRoom cr = factory.getClassDAO().getClass(rs.getInt("class_id"));
            List <Transaction> transactionDAO = factory.getTransactionDAO().getTransactionByUser(id);
            result = new Student(rs.getInt("student_id"), ud, cr, getStudentInventory(id), transactionDAO);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getErrorCode());
        }
        return result;
    }

    @Override
    public List <Student> getAllStudents() {
        List<Student> students = new ArrayList <>();
        String query = "SELECT * FROM student INNER JOIN codecooler ON student.student.id = codecooler.codecooler.id";
        ResultSet results = factory.execQuery(query);
        try {
            while(results.next()) {

                students.add(new Student())
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getSQLState());
        }
    }

    private Inventory getStudentInventory(int id) {
        List <Item> items = new ArrayList <>();
        String query = "SELECT * FROM inventory INNER JOIN item ON inventory.item_id = item.item_id WHERE student_id = ?";
        try {
            ResultSet rs = factory.execQuery(query, id);
            while (rs.next()) {
                items.add(
                        new Item(
                                rs.getInt("item_id"),
                                rs.getString("name"),
                                rs.getString("decription"),
                                rs.getInt("price"),
                                new Category(rs.getString("category")
                                )
                        )
                );
            }
        } catch (SQLException e) {
            System.out.println("Inventory Error: " + e.getSQLState());
        }
        return new Inventory(id, items);
    }
}

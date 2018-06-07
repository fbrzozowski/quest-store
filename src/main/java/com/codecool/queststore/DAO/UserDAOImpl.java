package com.codecool.queststore.DAO;

import com.codecool.queststore.model.user.AccountType;
import com.codecool.queststore.model.user.Mentor;
import com.codecool.queststore.model.user.UserDetails;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    private DAOFactory factory;

    public UserDAOImpl(DAOFactory factory) {
        this.factory = factory;
    }

    /*
	id serial PRIMARY KEY,
    first_name text,
    last_name text,
    email text,
    login text,
    password text,
    account_type text
     */
    @Override
    public void add(UserDetails userDetails) {
        String query = "INSER INTO codecooler VALUES (?,?,?,?,?)";
        factory.execQuery(query,
                userDetails.getFirstName(),
                userDetails.getLastName(),
                userDetails.getEmail(),
                userDetails.getPassword(),
                userDetails.getAccountType().toString()
        );
    }

    @Override
    public void remove(int id) {
        String query = "DELETE FROM codecooler WHERE id = ?";
        factory.execQuery(query, String.valueOf(id));
    }

    @Override
    public void update(UserDetails userDetails) {
        String query = "UPDATE codecooler SET first_name = ?, last_name = ?, email = ?, login = ?, password = ?, account_type = ? WHERE id = ?";
        factory.execQuery(query,
                userDetails.getFirstName(),
                userDetails.getLastName(),
                userDetails.getEmail(),
                userDetails.getLogin(),
                userDetails.getPassword(),
                String.valueOf(userDetails.getAccountType()),
                String.valueOf(userDetails.getId())
        );
    }

    @Override
    public UserDetails getUser(int id) {
        UserDetails result = null;
        String query = "SELECT * FROM codecooler WHERE id = " + id;
        ResultSet resultSet = factory.execQuery(query);
        //TODO: Extract to a method (lines 68-75 & 97-104)
        try {
            result = new UserDetails(
                    resultSet.getInt("id"),
                    resultSet.getString("first_name"),
                    resultSet.getString("last_name"),
                    resultSet.getString("email"),
                    resultSet.getString("login"),
                    resultSet.getString("password"),
                    getAccTypeValueOf(resultSet.getString("account_type")));

        } catch (SQLException e) {
            e.getErrorCode();
        }
        return result;
    }

    @Override
    public List<UserDetails> getAllStudents(Mentor mentor) {
        //TODO: Implement!
        return null;
    }

    @Override
    public List<UserDetails> getAll() {
        List<UserDetails> userDetails = new ArrayList<>();
        String query = "SELECT * FROM codecooler";
        ResultSet resultSet = factory.execQuery(query);

        try {
            while (resultSet.next()) {
                userDetails.add(new UserDetails(
                        resultSet.getInt("id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("email"),
                        resultSet.getString("login"),
                        resultSet.getString("password"),
                        getAccTypeValueOf(resultSet.getString("account_type"))));
            }
        } catch (SQLException e) {
            e.getErrorCode();
        }
        return userDetails;
    }

    private AccountType getAccTypeValueOf(String s) {
        AccountType type = null;
        switch (s) {
            case "ADMIN":
                type = AccountType.ADMIN;
                break;
            case "MENTOR":
                type = AccountType.MENTOR;
                break;
            case "STUDENT":
                type = AccountType.STUDENT;
                break;
        }
        return type;
    }
}

package com.codecool.queststore.DAO;

import java.sql.Connection;
import java.sql.ResultSet;

public abstract class DAOFactory {
    public abstract ItemDAO getItemDAO();
    public abstract TransactionDAO getTransactionDAO();
    public abstract ResultSet execQuery(String query);
    public abstract ResultSet execQuery(String query, String ... parameters);
    public abstract ResultSet execQuery(String query, int numberParameter, String ... parameters);
    public abstract ResultSet execQueryInt(String sqlQuery, int...numberParameter);
    public abstract Connection getConnection();
    public abstract void closeConnection();

    public static DAOFactory getDAOFactory() {
        return new DAOFactoryImpl();
    }
}

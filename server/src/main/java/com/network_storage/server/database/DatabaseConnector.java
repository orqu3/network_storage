package com.network_storage.server.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnector {
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/network_storage_db?serverTimezone=Europe/Moscow";
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "root";

    private static Connection connection;
    private static Statement statement;

    private static final String folder = "_folder/";
    private List<String> activeUsers = new ArrayList<>();

    public void connect() {
        if (connection == null) {
            try {
                Class.forName(DB_DRIVER);
                connection = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
                statement = connection.createStatement();
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
            try {
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }

    public String getFolder(String logPass){
        try {
            String[] logPassArr = logPass.split(" ");
            String sqlQuery = "SELECT folder FROM users WHERE login = '"
                    + logPassArr[0] + "'";
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            return resultSet.getString(1);
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean getAuth(String logPass){
        boolean result = false;
        try {
            String[] logPassArr = logPass.split(" ");
            String sqlQuery = "SELECT * FROM users WHERE login = '"
                    + logPassArr[0] + "' AND password = '" + logPassArr[1] + "'";
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (resultSet.next() && !activeUsers.contains(logPassArr[0])) {
                result = true;
                activeUsers.add(logPassArr[0]);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return result;
    }

    public boolean getReg(String logPass){
        int result = 0;
        try {
            if (!getAuth(logPass)){
                String[] logPassArr = logPass.split(" ");
                String sqlQuery = "INSERT INTO users (login, password, folder) VALUES ('"
                        + logPassArr[0] + "', '" + logPassArr[1] +"', '" + logPassArr[0] + folder + "')";
                result = statement.executeUpdate(sqlQuery);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return result == 1;
    }

    public void removeUser(String logPass){
        String[] logPassArr = logPass.split(" ");
        activeUsers.remove(logPassArr[0]);
    }
}


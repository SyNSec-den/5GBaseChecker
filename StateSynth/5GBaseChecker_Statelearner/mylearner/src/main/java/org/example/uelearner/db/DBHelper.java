package org.example.uelearner.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBHelper {
    static final String url = "jdbc:sqlite:my_database.sqlite";
    static Connection dbConnection = null;

    public static Connection getConnection() {
        try {
            if (DBHelper.dbConnection == null || DBHelper.dbConnection.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                DBHelper.dbConnection = DriverManager.getConnection(url);
            }
        } catch (Exception e) {
            System.out.println("DB Connection Error!");
            e.printStackTrace();
        }
        if (DBHelper.dbConnection == null) {
            System.out.println("***** IN DBHelper.getConnection(): CONNECTION NULL *****");
            System.exit(0); // for testing
        }
        return DBHelper.dbConnection;
    }
}

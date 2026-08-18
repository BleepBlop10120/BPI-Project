package com.banking.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String PROP_URL = "db.url";
    private static final String PROP_USERNAME = "db.username";
    private static final String PROP_PASSWORD = "db.password";

    private DBConnection() {
    }

public static Connection getConnection() throws SQLException {
    PropertyLoader config = PropertyLoader.getInstance();
    String url = config.getProperty(PROP_URL);
    String username = config.getProperty(PROP_USERNAME);
    String password = config.getProperty(PROP_PASSWORD);

    try {
        Connection conn = DriverManager.getConnection(url, username, password);
        return conn;
    } catch (SQLException e) {
        System.out.println("Connection Failed!");
        throw e;
    }

}

}

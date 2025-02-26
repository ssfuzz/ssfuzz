package org.example;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JDBCtest {
    public static void main(String[] args) {
        Connection con = null;

        Properties properties = new Properties();
        try (InputStream input = JDBCtest.class.getClassLoader().getResourceAsStream("jdbc.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find jdbc.properties");
                return;
            }

            properties.load(input); 

            String driver = properties.getProperty("driverClass");
            String url = properties.getProperty("url");
            String user = properties.getProperty("user");
            String password = properties.getProperty("password");

            Class.forName(driver);

            con = DriverManager.getConnection(url, user, password);
            if (!con.isClosed()) {
                System.out.println("Connection established successfully!");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
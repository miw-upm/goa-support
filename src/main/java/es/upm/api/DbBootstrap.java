package es.upm.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbBootstrap {
    public static void createDatabase() {
        try {
            String host = System.getenv("DB_HOST");
            String port = System.getenv("DB_PORT");
            String user = System.getenv("POSTGRES_USER");
            String pass = System.getenv("POSTGRES_PASSWORD");
            String dbName = System.getenv("DB_NAME");

            String url = "jdbc:postgresql://" + host + ":" + port + "/postgres";

            try (Connection conn = DriverManager.getConnection(url, user, pass);
                 Statement stmt = conn.createStatement()) {

                stmt.execute("CREATE DATABASE " + dbName);
                System.out.println("Database created or already existed: " + dbName);
            }
        } catch (Exception e) {
            System.out.println("Error creating database: " + e.getMessage());
        }
    }
}
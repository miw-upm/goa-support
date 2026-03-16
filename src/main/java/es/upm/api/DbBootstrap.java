package es.upm.api;

import javax.annotation.processing.Generated;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.logging.Logger;

@Generated("DbBootstrap not relevant for coverage")

public class DbBootstrap {

    private static final Logger LOGGER = Logger.getLogger(DbBootstrap.class.getName());

    private DbBootstrap() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

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
                LOGGER.info("Database created or already existed: " + dbName);
            }
        } catch (Exception e) {
            LOGGER.severe("Error creating database: " + e.getMessage());
        }
    }
}
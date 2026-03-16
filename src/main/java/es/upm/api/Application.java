package es.upm.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class Application {
    public static void main(String[] args) {
        // Create database before initializing application
        DbBootstrap.createDatabase();
        SpringApplication.run(Application.class, args);
    }
}

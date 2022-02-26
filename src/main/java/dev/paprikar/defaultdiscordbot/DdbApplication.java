package dev.paprikar.defaultdiscordbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * The main application class.
 */
@SpringBootApplication
public class DdbApplication {

    /**
     * The entry point of the application.
     *
     * @param args
     *         the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DdbApplication.class, args);
    }
}

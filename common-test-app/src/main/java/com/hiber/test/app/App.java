package com.hiber.test.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * An application that uses various supporting classes, primarily related to infrastructure and integration with external
 * services, and is a subject of various tests exercising if everything is set up correctly.
 */
@SpringBootApplication
public class App {
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
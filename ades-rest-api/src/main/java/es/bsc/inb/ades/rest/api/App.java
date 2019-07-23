package es.bsc.inb.ades.rest.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan("es.bsc.inb.ades")
public class App {

	public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
	
}
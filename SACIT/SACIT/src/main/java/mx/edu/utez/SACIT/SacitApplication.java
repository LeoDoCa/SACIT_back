package mx.edu.utez.SACIT;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SacitApplication {

	public static void main(String[] args) {
		SpringApplication.run(SacitApplication.class, args);
	}

}

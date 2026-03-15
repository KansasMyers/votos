package br.com.cooperativa.votos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VotosApplication {

	public static void main(String[] args) {
		SpringApplication.run(VotosApplication.class, args);
	}

}

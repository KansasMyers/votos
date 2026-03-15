package br.com.cooperativa.votos;

import org.springframework.boot.SpringApplication;

import br.com.cooperativa.votos.config.TestcontainersConfiguration;

public class TestVotosApplication {

	public static void main(String[] args) {
		SpringApplication.from(VotosApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

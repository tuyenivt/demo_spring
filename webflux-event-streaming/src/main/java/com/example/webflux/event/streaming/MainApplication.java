package com.example.webflux.event.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class MainApplication {

	Logger logger = LoggerFactory.getLogger(MainApplication.class);

	@Bean
	WebClient getWebClient() {
		return WebClient.create("http://localhost:8080");
	}

	@Bean
	CommandLineRunner demo(WebClient client) {
		return args -> {
			client.get()
				.uri("/temperatures")
				.accept(MediaType.TEXT_EVENT_STREAM)
				.retrieve()
				.bodyToFlux(String.class)
				.map(s -> String.valueOf(s))
				.subscribe(msg -> logger.info(msg));
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}
}

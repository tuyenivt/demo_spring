package com.example.demo.controller;

import java.time.Duration;
import java.util.Random;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
public class TemperatureController {
	Logger logger = LoggerFactory.getLogger(TemperatureController.class);

	@GetMapping(value = "/temperatures", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> getTemperature() {
		Random r = new Random();
		int low = 0;
		int high = 50;
		return Flux.fromStream(Stream.generate(() -> r.nextInt(high-low) + low)
				.map(s -> String.valueOf(s))
				.peek((msg) -> logger.info(msg)))
				.map(s -> String.valueOf(s))
				.delayElements(Duration.ofSeconds(1)); // delay for every item sent to the client
	}
}

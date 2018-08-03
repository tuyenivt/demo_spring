package com.example.webflux.reactive.mongodb.controller;

import java.time.Duration;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.webflux.reactive.mongodb.entity.Tweet;
import com.example.webflux.reactive.mongodb.repository.TweetRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping(value = "/api")
public class TweetController {

	@Autowired
	private TweetRepository repository;
	
	// Flux implement the Publisher interface provided by Reactive Streams
	// Flux is used to represent a stream of 0..N elements
	@GetMapping("/tweets")
	public Flux<Tweet> getAllTweets() {
		return repository.findAll().delayElements(Duration.ofMillis(100));
	}
	
	// Mono implement the Publisher interface provided by Reactive Streams
	// Mono is used to represent a stream of 0..1 elements
	@PostMapping("/tweets")
	public Mono<Tweet> createTweets(@Valid @RequestBody Tweet tweet) {
		return repository.save(tweet);
	}
	
	@GetMapping("/tweets/{id}")
	public Mono<ResponseEntity<Tweet>> getTweetById(@PathVariable(value = "id") String tweetId) {
		return repository.findById(tweetId)
				.map(savedTweet -> ResponseEntity.ok(savedTweet))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PutMapping("/tweets/{id}")
	public Mono<ResponseEntity<Tweet>> updateTweets(@PathVariable(value = "id") String tweetId,
			@Valid @RequestBody Tweet tweet) {
		return repository.findById(tweetId)
				.flatMap(existingTweet -> {
					existingTweet.setText(tweet.getText());
					return repository.save(existingTweet);
				})
				.map(updatedTweet -> ResponseEntity.ok(updatedTweet))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping("/tweets/{id}")
	public Mono<ResponseEntity<Void>> deleteTweets(@PathVariable(value = "id") String tweetId) {
		return repository.findById(tweetId)
				.flatMap(existingTweet -> repository.delete(existingTweet)
						.then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@GetMapping("/tweets/search")
	public Flux<Tweet> getTweetsByName(@RequestParam(value = "t") String text) {
		return repository.findByText(text).delayElements(Duration.ofMillis(100));
	}
	
	// Tweets are Sent to the client as Server Sent Events
	// A browser client has no way to consume a stream other than using Server-Sent-Events or WebSocket
	// Non-browser clients can request a stream of JSON by setting the Accept header to application/stream+json
	// and the response will be a stream of JSON similar to Server-Sent-Events but without extra formatting
	// {"id":"5b6022132638f232300ec8f6","text":"This is a Test Tweet 1","createdAt":1533026835114}
	// {"id":"5b60224b2638f20fd451cdd0","text":"This is a Test Tweet 2","createdAt":1533026890557}
	@GetMapping(value = "/stream/tweets", produces = MediaType.TEXT_EVENT_STREAM_VALUE) // text/event-stream
	public Flux<Tweet> streamAllTweets() {
		return repository.findAll().delayElements(Duration.ofMillis(100));
	}
}

package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.entity.Tweet;
import com.example.repository.TweetRepository;

import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MainApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private TweetRepository repository;

	@Test
	public void testCreateTweet() {
		Tweet tweet = new Tweet("This is a Test Tweet");
		client.post()
			.uri("/api/tweets")
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.accept(MediaType.APPLICATION_JSON_UTF8)
			.body(Mono.just(tweet), Tweet.class)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
			.expectBody()
			.jsonPath("$.id").isNotEmpty()
			.jsonPath("$.text").isEqualTo("This is a Test Tweet");
	}

	@Test
	public void testGetAllTweets() {
		client.get()
			.uri("/api/tweets")
			.accept(MediaType.APPLICATION_JSON_UTF8)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
			.expectBodyList(Tweet.class);
	}

	@Test
	public void testGetSingleTweet() {
		Tweet tweet = repository.save(new Tweet("This is a Test Tweet")).block();
		client.get()
			.uri("/api/tweets/{id}", Collections.singletonMap("id", tweet.getId()))
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.consumeWith(response -> assertThat(response.getResponseBody()).isNotNull());
	}

	@Test
	public void testUpdateTweet() {
		Tweet tweet = repository.save(new Tweet("Initial Tweet")).block();
		Tweet newTweetData = new Tweet("Updated Tweet");
		client.put()
			.uri("/api/tweets/{id}", Collections.singletonMap("id", tweet.getId()))
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.accept(MediaType.APPLICATION_JSON_UTF8)
			.body(Mono.just(newTweetData), Tweet.class)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
			.expectBody()
			.jsonPath("$.text", newTweetData.getText());
		
	}

	@Test
	public void testDeleteTweet() {
		Tweet tweet = repository.save(new Tweet("This is a Test Tweet")).block();
		client.delete()
			.uri("/api/tweets/{id}", Collections.singletonMap("id", tweet.getId()))
			.exchange()
			.expectStatus().isOk();
	}

	@Test
	public void testGetTweetsByName() {
		Tweet tweet = repository.save(new Tweet("This is a Test Tweet")).block();
		client.get()
			.uri("/api/tweets/search?t={text}", Collections.singletonMap("text", tweet.getText()))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(Tweet.class);
	}
}

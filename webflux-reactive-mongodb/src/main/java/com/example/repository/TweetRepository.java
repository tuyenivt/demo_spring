package com.example.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Tweet;

import reactor.core.publisher.Flux;

@Repository
public interface TweetRepository extends ReactiveMongoRepository<Tweet, String> {

	Flux<Tweet> findByText(String text);
	
}

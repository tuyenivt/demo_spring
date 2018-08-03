package com.example.webflux.reactive.mongodb.entity;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tweets")
public class Tweet {

	@Id
	private String id;

	@NotBlank
	@Size(max = 150)
	private String text;

	@NotNull
	private Date createdAt = new Date();

	public Tweet() {
	}

	public Tweet(@NotBlank @Size(max = 150) String text) {
		this.text = text;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "Tweet [id=" + id + ", text=" + text + ", createdAt=" + createdAt + "]";
	}

}

package com.sample.kafka;

public class FavouriteColourStreamsAppRunner {

    public static void main(String[] args) {
        var app = new FavouriteColourStreamsApp("localhost:9092");
        // app.transform("favouritecolour-streams-input", "favouritecolour-streams-output");
        app.transformWithIntermediaryTopic("favouritecolour-streams-input", "favouritecolour-streams-output");
    }
}

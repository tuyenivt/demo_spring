package com.example.ratelimiting.ratelimit;

public enum RefillStrategy {
    /**
     * Tokens refill all at once after the interval elapses.
     */
    INTERVALLY,

    /**
     * Tokens are added gradually over the interval, preventing boundary bursts.
     */
    GREEDY
}

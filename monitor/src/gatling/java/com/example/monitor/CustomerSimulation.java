package com.example.monitor;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.rampConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class CustomerSimulation extends Simulation {
    HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080");

    ScenarioBuilder scn = scenario("Concurrent requests")
            .exec(http("request_1").get("/customers"))
            .exec(http("request_2").get("/customers/transform"));

    {
        setUp(scn.injectClosed(rampConcurrentUsers(5).to(100).during(160))).protocols(httpProtocol);
    }
}

package com.example.monitor;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class CustomerSimulation extends Simulation {
    HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080");

    ScenarioBuilder scn = scenario("Concurrent requests")
            .exec(http("request_1").get("/customers"))
            .exec(http("request_2").get("/customers/transform"))
            .exec(http("request_3").get("/customers/unreliable"));

    {
        setUp(scn.injectClosed(rampConcurrentUsers(5).to(100).during(160)))
                .protocols(httpProtocol)
                .assertions(
                        global().successfulRequests().percent().gte(95.0),
                        global().responseTime().percentile3().lt(5000),
                        forAll().responseTime().mean().lt(1000)
                );
    }
}

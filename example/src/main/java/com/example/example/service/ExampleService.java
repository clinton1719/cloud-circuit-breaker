package com.example.example.service;

import com.cloudcb.annotation.CloudCircuitBreaker;
import org.springframework.stereotype.Service;

/**
 * @author Clinton Fernandes
 */
@Service
public class ExampleService {

    @CloudCircuitBreaker(function = "callDownstreamService", fallback = "fallbackService")
    public String callDownstreamService() {
        System.err.println("  ---> SIMULATING FAILURE for myDownstreamApiCall!");
        throw new RuntimeException("Simulated External API connection timeout / error!");
    }

    public String fallbackService() {
        return "Fallback!";
    }
}

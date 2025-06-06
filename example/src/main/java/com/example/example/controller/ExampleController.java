package com.example.example.controller;

import com.cloudcb.annotation.CloudCircuitBreaker;
import com.example.example.service.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Clinton Fernandes
 */
@RestController()
public class ExampleController {

    @Autowired
    private ExampleService exampleService;

    @GetMapping("/test")
    public String testBreaker() {
        return exampleService.callDownstreamService();
    }
}

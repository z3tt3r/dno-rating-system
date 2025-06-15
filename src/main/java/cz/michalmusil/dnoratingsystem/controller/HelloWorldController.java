package cz.michalmusil.dnoratingsystem.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloWorldController { // Doporučuji název HelloWorldController

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot with Gradle";
    }
}
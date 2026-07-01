package com.example.e_commerce.controller;

import com.example.e_commerce.service.RedisTestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private final RedisTestService redisTestService;

    public TestController(RedisTestService redisTestService) {
        this.redisTestService = redisTestService;
    }

    @GetMapping("/redis")
    public String testRedis() {
        redisTestService.test();
        return "OK";
    }
}

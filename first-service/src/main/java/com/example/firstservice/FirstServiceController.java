package com.example.firstservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/first-service")
public class FirstServiceController {

    @Autowired
    Environment env;
    public FirstServiceController(Environment env){
        this.env = env;
    }

    @GetMapping("/welcome")
    public String welcome(){

        return "Welcome to the First serivce.";
    }

    @GetMapping("/message")
    public String message(@RequestHeader("first-request")String header) {
        log.info(header);
        return "hello world in first-service";
    }

    @GetMapping("/check")
    public String check(HttpServletRequest request){

        log.info("Server port={}",request.getServerPort());

        return String.format("Hi,there Ths is a message from First service on PORT %s",
                env.getProperty("local.server.port"));
    }
}

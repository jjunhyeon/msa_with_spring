package com.example.userservice.vo;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
//@AllArgsConstructor // argument를 다 가지고 있는 생성자
//@NoArgsConstructor // default 생성자
public class Greeting {

    @Value("${greeting.message}")
    private String message;
}

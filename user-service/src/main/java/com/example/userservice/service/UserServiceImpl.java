package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.error.FeignErrorDecoder;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseOrder;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    BCryptPasswordEncoder passwordEncoder;

    RestTemplate restTemplate;

    Environment env;

    OrderServiceClient orderServiceClient;
    CircuitBreakerFactory circuitBreakerFactory;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(username);

        if (userEntity == null) {
            throw new UsernameNotFoundException(username);
        }

        return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(),
                true, true, true, true,
                new ArrayList<>());
    }

    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           Environment env,
                           RestTemplate restTemplate,
                           OrderServiceClient orderServiceClient,
                           CircuitBreakerFactory circuitBreakerFactory) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.env =env;
        this.restTemplate = restTemplate;
        this.orderServiceClient =orderServiceClient;
        this.circuitBreakerFactory=circuitBreakerFactory;
    }


    @Override
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString()); // 랜덤 유효아이디 생성

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = mapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));
        userRepository.save(userEntity);

        UserDto returnUserdto = mapper.map(userEntity, UserDto.class);

        return returnUserdto;
    }

    @Override
    public UserDto getUserByUserId(String userId) {

        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found");
        }

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

//        List<ResponseOrder> orders = new ArrayList<>();
//        String orderUrl = String.format(env.getProperty("order_service.url"),userId);
//        ResponseEntity<List<ResponseOrder>> orderListResponse =
//                restTemplate.exchange(orderUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<ResponseOrder>>() {
//        });
//
//        List<ResponseOrder> orderList = orderListResponse.getBody();

        /* using a Feign Client */
        //List<ResponseOrder> orderList = orderServiceClient.getOrders(userId);
//        List<ResponseOrder> orderList = null;
//        try{
//            orderList = orderServiceClient.getOrders(userId);
//        }catch(FeignException ex){
//            log.error(ex.getMessage());
//        }
        /* Errordecoder */
//       List<ResponseOrder> orderList = orderServiceClient.getOrders(userId);

        log.info("Before call orders microservice");
        CircuitBreaker circuitBreaker =circuitBreakerFactory.create("circuitBreaker");
        List<ResponseOrder> orderList = circuitBreaker.run(() -> orderServiceClient.getOrders(userId),
            throwable -> new ArrayList<>());
        log.info("After call orders microservice");
        userDto.setOrders(orderList);

        return userDto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) {
            throw new UsernameNotFoundException(email);
        }
        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
        return userDto;
    }

}

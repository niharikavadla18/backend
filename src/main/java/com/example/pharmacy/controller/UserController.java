package com.example.pharmacy.controller;

import com.example.pharmacy.entity.User;
import com.example.pharmacy.security.JwtService;
import com.example.pharmacy.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    private final JwtService jwtService;

    public UserController(UserService userService,
                          JwtService jwtService) {

        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {

        return userService.register(user);
    }

    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestBody User user) {

        User loggedInUser =
                userService.login(
                        user.getEmail(),
                        user.getPassword()
                );

        String token =
                jwtService.generateToken(
                        loggedInUser.getEmail(),
                        loggedInUser.getRole()
                );

        Map<String, Object> response =
                new HashMap<>();

        response.put("id", loggedInUser.getId());
        response.put("name", loggedInUser.getName());
        response.put("email", loggedInUser.getEmail());
        response.put("role", loggedInUser.getRole());
        response.put("token", token);

        return response;
    }
}
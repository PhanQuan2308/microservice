package org.example.userservice.controller;


import jakarta.servlet.http.HttpServletRequest;

import org.example.userservice.entity.User;
import org.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/token/blacklist/check")
    public ResponseEntity<Boolean> isTokenBlacklisted(@RequestParam("token") String token) {
        boolean isBlacklisted = userService.isTokenBlacklisted(token);
        return ResponseEntity.ok(isBlacklisted);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        try {
            String result = userService.registerUser(user.getUsername(), user.getPassword());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        String token = userService.loginUser(user.getUsername(), user.getPassword());
        return ResponseEntity.ok(token);
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        if(header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            userService.logout(token);
            return ResponseEntity.ok().build();


        }
        return ResponseEntity.badRequest().body("Token is missing");

    }
}

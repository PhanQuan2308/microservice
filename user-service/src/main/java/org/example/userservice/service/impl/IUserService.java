package org.example.userservice.service.impl;

public interface IUserService {
    boolean usernameExists(String username);
    boolean isTokenBlacklisted(String token);
    String registerUser(String username, String password);
    String loginUser(String username, String password);
    String logout(String token);
}
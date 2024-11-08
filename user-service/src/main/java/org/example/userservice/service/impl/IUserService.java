package org.example.userservice.service.impl;

import org.example.userservice.dto.UserDTO;
import org.example.userservice.entity.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    boolean usernameExists(String username);
    boolean isTokenBlacklisted(String token);
    String registerUser(String username, String password);
    String loginUser(String username, String password);
    String logout(String token);

    List<UserDTO> getAllUsers();
    Optional<UserDTO> getUserById(Long id);
    String updateUser(User user);
    String deleteUser(Long id);
}
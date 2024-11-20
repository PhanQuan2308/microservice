package org.example.userservice.service.impl;


import org.example.userservice.dto.UserDTO;
import org.example.userservice.entity.TokenBlackList;
import org.example.userservice.entity.User;
import org.example.userservice.repository.TokenBlacklistRepository;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.service.UserService;
import org.example.userservice.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setRole(user.getRole());
        return userDTO;
    }

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;
    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public boolean isTokenBlacklisted(String token) {
        boolean exists = tokenBlacklistRepository.findByToken(token).isPresent();
        System.out.println("Token " + token + " in blacklist: " + exists);
        return exists;
    }

    public String registerUser(String username, String password) {
        if (usernameExists(username)) {
            throw new IllegalArgumentException("Username is already in use");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setRole("ROLE_USER");
        userRepository.save(user);
        return "User registered successfully";
    }

    public String loginUser(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && bCryptPasswordEncoder.matches(password, user.get().getPassword())) {
            String userId = String.valueOf(user.get().getId());
            String role = user.get().getRole();
            return jwtUtil.generateToken(userId, username, role);
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }



    public String logout(String token) {
        System.out.println("Logging out with token: " + token);
        TokenBlackList blackList = new TokenBlackList();
        blackList.setToken(token);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 1);
        blackList.setExpiryDate(calendar.getTime());

        tokenBlacklistRepository.save(blackList);
        System.out.println("Token added to blacklist successfully");

        return "Logout successful";
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    public String updateUser(User user) {
        Optional<User> optionalUser = userRepository.findById(user.getId());
        if(optionalUser.isPresent()) {
            User existingUser = optionalUser.get();

            if(existingUser.getUsername().equals(user.getUsername())) {
                existingUser.setUsername(user.getUsername());
            }
            if(existingUser.getPassword().equals(user.getPassword())) {
                existingUser.setPassword(user.getPassword());
            }
            if(existingUser.getRole().equals(user.getRole())) {
                existingUser.setRole(user.getRole());
            }

            userRepository.save(existingUser);
            return "User updated successfully";

        } else {
            throw new RuntimeException("User not found");
        }
    }

    public String deleteUser(Long id) {
        if(userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return "User deleted successfully";
        }
        else {
            throw new RuntimeException("User not found");
        }
    }

}

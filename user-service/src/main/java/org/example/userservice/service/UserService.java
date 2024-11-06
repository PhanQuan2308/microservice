package org.example.userservice.service;


import org.example.userservice.entity.TokenBlackList;
import org.example.userservice.entity.User;
import org.example.userservice.repository.TokenBlacklistRepository;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.service.impl.IUserService;
import org.example.userservice.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

@Service
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

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

}

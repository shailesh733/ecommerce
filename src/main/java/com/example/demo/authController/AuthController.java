package com.example.demo.authController;


import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.jwt.JwtUtil;
import com.example.demo.repo.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public Map<String, String> signUp(@RequestBody User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole(Role.USER); // default role
        }
        userRepository.save(user);
        return Map.of("message", "User registered successfully as " + user.getRole());
    }

    @PostMapping("/signin")
    public Map<String, String> signIn(@RequestBody User user) {
        User dbUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (encoder.matches(user.getPassword(), dbUser.getPassword())) {
            String token = jwtUtil.generateToken(dbUser.getEmail());
            return Map.of("token", token);
        } else {
            throw new RuntimeException("Invalid password");
        }
    }
}


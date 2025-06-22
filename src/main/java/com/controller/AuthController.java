package com.controller;

import com.dto.AuthRequest;
import com.dto.AuthResonse;
import com.model.AppUser;
import com.model.Role;
import com.repository.AppUserRepository;
import com.service.CustomUserDetailsService;
import com.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Web Form Endpoints
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                              @RequestParam(value = "success", required = false) String success,
                              Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (success != null) {
            model.addAttribute("success", "Login successful!");
        }
        return "login.html";
    }

    @PostMapping("/login")
    public String processLoginForm(@RequestParam String username, @RequestParam String password, 
                                 Model model, HttpSession session) {
        try {
            Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            session.setAttribute("username", username);
            
            // Check if user has admin role
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("admin"))) {
                return "redirect:/admin/dashboard";
            }
            
            return "redirect:/home?success=true";
        } catch (Exception e) {
            model.addAttribute("error", "Invalid username or password");
            return "login.html";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "register.html";
    }

    @PostMapping("/register")
    public String processRegisterForm(@RequestParam String username, @RequestParam String password, Model model) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "Username cannot be empty");
            return "register.html";
        }
        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Password cannot be empty");
            return "register.html";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters long");
            return "register.html";
        }

        // Check if username exists
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already taken");
            return "register.html";
        }

        // Create and save user
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.user);  // Set default role as user
        userRepository.save(user);

        // Redirect to login with success message
        return "redirect:/auth/login?registered=true";
    }

    // API Endpoints
    @PostMapping("/api/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username cannot be empty");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Password cannot be empty");
        }
        if (request.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body("Password must be at least 6 characters long");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken");
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.user);  // Set default role as user
        userRepository.save(user);
        
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
    try {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Load UserDetails from your custom service
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        // Pass UserDetails to your JWT utility method
        final String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResonse(token));
    } catch (BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authentication failed");
    }
}
}

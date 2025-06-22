package com.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import com.model.AppUser;
@Service
public class AuthService {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean login(String username, String rawPassword) {
        Optional<AppUser> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            AppUser user = optionalUser.get();
            return passwordEncoder.matches(rawPassword, user.getPassword());
        }
        return false;
    }
}

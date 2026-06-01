package com.example.pasir_ihor_kotenko.service;
import com.example.pasir_ihor_kotenko.model.User;
import com.example.pasir_ihor_kotenko.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Service
public class CurrentUserService {
    private final UserRepository userRepository;
    public CurrentUserService(UserRepository userRepository) { this.userRepository = userRepository; }
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony"));
    }
}

package com.example.pasir_ihor_kotenko.service;

import com.example.pasir_ihor_kotenko.dto.LoginDto;
import com.example.pasir_ihor_kotenko.dto.UserDto;
import com.example.pasir_ihor_kotenko.exception.UserAlreadyExistsException;
import com.example.pasir_ihor_kotenko.model.User;
import com.example.pasir_ihor_kotenko.repository.UserRepository;
import com.example.pasir_ihor_kotenko.security.JwtUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder encoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    public void register(UserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Użytkownik z emailem " + dto.getEmail() + " już istnieje");
        }
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "PLN");
        userRepository.save(user);
    }

    public String login(LoginDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Nieprawidłowy email lub hasło"));
        if (!encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new UsernameNotFoundException("Nieprawidłowy email lub hasło");
        }
        return jwtUtil.generateToken(user.getEmail(), user.getId());
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony: " + email));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList()
        );
    }
}

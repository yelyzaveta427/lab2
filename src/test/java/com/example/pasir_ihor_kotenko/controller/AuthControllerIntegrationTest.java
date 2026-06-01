package com.example.pasir_ihor_kotenko.controller;
import com.example.pasir_ihor_kotenko.dto.LoginDto;
import com.example.pasir_ihor_kotenko.dto.UserDto;
import com.example.pasir_ihor_kotenko.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TEST_PASSWORD = "SecurePassword123";
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }
    private String generateUniqueEmail() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8) + "@pk.pl";
    }
    private UserDto buildUserDto(String email) {
        UserDto dto = new UserDto();
        dto.setEmail(email);
        dto.setPassword(TEST_PASSWORD);
        dto.setCurrency("PLN");
        return dto;
    }
    @Test
    @DisplayName("register user")
    void shouldRegisterNewUser() throws Exception {
        String email = generateUniqueEmail();
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(buildUserDto(email)))).andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Użytkownik zarejestrowany pomyślnie"));
    }
    @Test
    @DisplayName("login returns jwt")
    void shouldLoginAndReturnJwtToken() throws Exception {
        String email = generateUniqueEmail();
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(buildUserDto(email)))).andExpect(status().isOk());
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail(email);
        loginDto.setPassword(TEST_PASSWORD);
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginDto))).andExpect(status().isOk()).andExpect(jsonPath("$.token").exists()).andExpect(jsonPath("$.token").value(matchesPattern("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*$")));
    }
    @Test
    @DisplayName("login wrong password")
    void shouldReturn401WhenLoginWithWrongPassword() throws Exception {
        String email = generateUniqueEmail();
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(buildUserDto(email)))).andExpect(status().isOk());
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail(email);
        loginDto.setPassword("WrongPassword");
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginDto))).andExpect(status().isUnauthorized());
    }
    @Test
    @DisplayName("register invalid email")
    void shouldReturn400WhenRegisterWithInvalidEmail() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setEmail("invalid-email");
        userDto.setPassword("Password123");
        userDto.setCurrency("PLN");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(userDto))).andExpect(status().isBadRequest());
    }
    @Test
    @DisplayName("register duplicate email")
    void shouldReturn409WhenRegisterWithDuplicateEmail() throws Exception {
        String email = generateUniqueEmail();
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(buildUserDto(email)))).andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(buildUserDto(email)))).andExpect(status().isConflict()).andExpect(jsonPath("$.error").value(containsString("już istnieje")));
    }
}

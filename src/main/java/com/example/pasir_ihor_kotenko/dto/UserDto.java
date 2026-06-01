package com.example.pasir_ihor_kotenko.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDto {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private String currency = "PLN";
}

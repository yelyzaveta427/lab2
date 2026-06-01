package com.example.pasir_ihor_kotenko.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public class GroupDTO {
    @NotBlank(message = "Nazwa grupy jest wymagana")
    @Size(max = 100, message = "Nazwa grupy może mieć maksymalnie 100 znaków")
    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

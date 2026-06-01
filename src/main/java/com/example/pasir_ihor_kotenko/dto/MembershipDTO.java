package com.example.pasir_ihor_kotenko.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public class MembershipDTO {
    @NotNull(message = "ID grupy jest wymagane")
    private Long groupId;
    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Niepoprawny email")
    private String email;
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

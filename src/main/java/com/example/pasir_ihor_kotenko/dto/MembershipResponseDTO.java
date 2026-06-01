package com.example.pasir_ihor_kotenko.dto;
public class MembershipResponseDTO {
    private Long id;
    private Long groupId;
    private Long userId;
    private String email;
    public MembershipResponseDTO(Long id, Long groupId, Long userId, String email) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.email = email;
    }
    public Long getId() { return id; }
    public Long getGroupId() { return groupId; }
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
}

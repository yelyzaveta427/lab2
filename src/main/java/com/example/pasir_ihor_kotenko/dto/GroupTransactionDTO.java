package com.example.pasir_ihor_kotenko.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
public class GroupTransactionDTO {
    @NotNull(message = "ID grupy jest wymagane")
    private Long groupId;
    @NotNull(message = "Kwota jest wymagana")
    @DecimalMin(value = "0.01", message = "Kwota musi być większa od 0")
    private Double amount;
    @NotBlank(message = "Typ jest wymagany")
    @Pattern(regexp = "INCOME|EXPENSE", message = "Typ musi być INCOME albo EXPENSE")
    private String type;
    @NotBlank(message = "Tytuł jest wymagany")
    @Size(max = 255, message = "Tytuł może mieć maksymalnie 255 znaków")
    private String title;
    private List<Long> selectedUserIds;
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<Long> getSelectedUserIds() { return selectedUserIds; }
    public void setSelectedUserIds(List<Long> selectedUserIds) { this.selectedUserIds = selectedUserIds; }
}

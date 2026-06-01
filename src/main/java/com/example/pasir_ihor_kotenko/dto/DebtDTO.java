package com.example.pasir_ihor_kotenko.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
public class DebtDTO {
    @NotNull(message = "ID grupy jest wymagane")
    private Long groupId;
    @NotNull(message = "ID dłużnika jest wymagane")
    private Long debtorId;
    @NotNull(message = "ID wierzyciela jest wymagane")
    private Long creditorId;
    @NotNull(message = "Kwota jest wymagana")
    @DecimalMin(value = "0.01", message = "Kwota musi być większa od 0")
    private Double amount;
    @NotBlank(message = "Tytuł jest wymagany")
    @Size(max = 255, message = "Tytuł może mieć maksymalnie 255 znaków")
    private String title;
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public Long getDebtorId() { return debtorId; }
    public void setDebtorId(Long debtorId) { this.debtorId = debtorId; }
    public Long getCreditorId() { return creditorId; }
    public void setCreditorId(Long creditorId) { this.creditorId = creditorId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}

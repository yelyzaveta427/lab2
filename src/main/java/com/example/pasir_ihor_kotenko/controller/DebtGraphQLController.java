package com.example.pasir_ihor_kotenko.controller;
import com.example.pasir_ihor_kotenko.dto.DebtDTO;
import com.example.pasir_ihor_kotenko.model.Debt;
import com.example.pasir_ihor_kotenko.service.DebtService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.util.List;
@Controller
public class DebtGraphQLController {
    private final DebtService debtService;
    public DebtGraphQLController(DebtService debtService) { this.debtService = debtService; }
    @QueryMapping
    public List<Debt> groupDebts(@Argument Long groupId) { return debtService.getGroupDebts(groupId); }
    @MutationMapping
    public Debt createDebt(@Argument @Valid DebtDTO input) { return debtService.createDebt(input); }
    @MutationMapping
    public Boolean deleteDebt(@Argument Long debtId) { return debtService.deleteDebt(debtId); }
    @MutationMapping
    public Debt markDebtAsPaid(@Argument Long debtId) { return debtService.markDebtAsPaid(debtId); }
    @MutationMapping
    public Debt confirmDebtPayment(@Argument Long debtId) { return debtService.confirmDebtPayment(debtId); }
}

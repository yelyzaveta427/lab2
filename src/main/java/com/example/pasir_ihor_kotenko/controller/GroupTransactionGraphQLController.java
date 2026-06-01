package com.example.pasir_ihor_kotenko.controller;
import com.example.pasir_ihor_kotenko.dto.GroupTransactionDTO;
import com.example.pasir_ihor_kotenko.model.Transaction;
import com.example.pasir_ihor_kotenko.model.User;
import com.example.pasir_ihor_kotenko.service.CurrentUserService;
import com.example.pasir_ihor_kotenko.service.GroupTransactionService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
@Controller
public class GroupTransactionGraphQLController {
    private final GroupTransactionService groupTransactionService;
    private final CurrentUserService currentUserService;
    public GroupTransactionGraphQLController(GroupTransactionService groupTransactionService, CurrentUserService currentUserService) {
        this.groupTransactionService = groupTransactionService;
        this.currentUserService = currentUserService;
    }
    @MutationMapping
    public Transaction addGroupTransaction(@Argument @Valid GroupTransactionDTO input) {
        User current = currentUserService.getCurrentUser();
        return groupTransactionService.addGroupTransaction(input, current);
    }
}

package com.example.pasir_ihor_kotenko.controller;

import com.example.pasir_ihor_kotenko.dto.BalanceDto;
import com.example.pasir_ihor_kotenko.dto.TransactionDTO;
import com.example.pasir_ihor_kotenko.model.Transaction;
import com.example.pasir_ihor_kotenko.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class TransactionGraphQLController {
    private final TransactionService transactionService;

    public TransactionGraphQLController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @QueryMapping
    public List<Transaction> transactions(
            @Argument String type,
            @Argument String tags,
            @Argument Double minAmount,
            @Argument Double maxAmount,
            @Argument String from,
            @Argument String to,
            @Argument String sortBy,
            @Argument String sortDir
    ) {
        LocalDateTime fromDate = from == null ? null : LocalDateTime.parse(from);
        LocalDateTime toDate = to == null ? null : LocalDateTime.parse(to);
        return transactionService.getAllFiltered(type, tags, minAmount, maxAmount, fromDate, toDate, sortBy, sortDir);
    }

    @QueryMapping
    public BalanceDto userBalance(@Argument Integer days) {
        return transactionService.getUserBalance(days);
    }

    @MutationMapping
    public Transaction addTransaction(@Argument("transactionDTO") @Valid TransactionDTO transactionDTO) {
        return transactionService.create(transactionDTO);
    }

    @MutationMapping
    public Transaction updateTransaction(@Argument Long id, @Argument("transactionDTO") @Valid TransactionDTO transactionDTO) {
        return transactionService.update(id, transactionDTO);
    }

    @MutationMapping
    public Boolean deleteTransaction(@Argument Long id) {
        transactionService.delete(id);
        return true;
    }
}

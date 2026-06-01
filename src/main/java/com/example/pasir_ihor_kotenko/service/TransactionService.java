package com.example.pasir_ihor_kotenko.service;

import com.example.pasir_ihor_kotenko.dto.TransactionDTO;
import com.example.pasir_ihor_kotenko.dto.BalanceDto;
import com.example.pasir_ihor_kotenko.model.Transaction;
import com.example.pasir_ihor_kotenko.model.TransactionType;
import com.example.pasir_ihor_kotenko.model.User;
import com.example.pasir_ihor_kotenko.repository.TransactionRepository;
import com.example.pasir_ihor_kotenko.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class TransactionService {
    private final TransactionRepository repository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony"));
    }

    public List<Transaction> getAll() {
        return repository.findByUser(getCurrentUser());
    }

    public List<Transaction> getAllFiltered(
            String type,
            String tags,
            Double minAmount,
            Double maxAmount,
            LocalDateTime from,
            LocalDateTime to,
            String sortBy,
            String sortDir
    ) {
        List<Transaction> transactions = repository.findByUser(getCurrentUser()).stream()
                .filter(t -> type == null || t.getType().name().equalsIgnoreCase(type))
                .filter(t -> tags == null || (t.getTags() != null && t.getTags().toLowerCase(Locale.ROOT).contains(tags.toLowerCase(Locale.ROOT))))
                .filter(t -> minAmount == null || t.getAmount() >= minAmount)
                .filter(t -> maxAmount == null || t.getAmount() <= maxAmount)
                .filter(t -> from == null || (t.getTimestamp() != null && !t.getTimestamp().isBefore(from)))
                .filter(t -> to == null || (t.getTimestamp() != null && !t.getTimestamp().isAfter(to)))
                .toList();
        Comparator<Transaction> comparator = "amount".equalsIgnoreCase(sortBy)
                ? Comparator.comparing(Transaction::getAmount)
                : Comparator.comparing(Transaction::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder()));
        List<Transaction> sorted = transactions.stream().sorted(comparator).toList();
        if ("desc".equalsIgnoreCase(sortDir)) {
            return sorted.reversed();
        }
        return sorted;
    }

    public Transaction getById(Long id) {
        Transaction transaction = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transakcja o id " + id + " nie istnieje"));
        if (!transaction.getUser().getId().equals(getCurrentUser().getId())) {
            throw new AccessDeniedException("Brak dostępu do tej transakcji");
        }
        return transaction;
    }

    public Transaction create(TransactionDTO dto) {
        Transaction transaction = new Transaction(
                dto.getAmount(),
                TransactionType.valueOf(dto.getType()),
                dto.getTags(),
                dto.getNotes(),
                getCurrentUser()
        );
        transaction.setTimestamp(LocalDateTime.now());
        return repository.save(transaction);
    }

    public Transaction update(Long id, TransactionDTO dto) {
        Transaction transaction = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transakcja o id " + id + " nie istnieje"));
        if (!transaction.getUser().getId().equals(getCurrentUser().getId())) {
            throw new AccessDeniedException("Brak dostępu do tej transakcji");
        }
        transaction.setAmount(dto.getAmount());
        transaction.setType(TransactionType.valueOf(dto.getType()));
        transaction.setTags(dto.getTags());
        transaction.setNotes(dto.getNotes());
        return repository.save(transaction);
    }

    public void delete(Long id) {
        Transaction transaction = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transakcja o id " + id + " nie istnieje"));
        if (!transaction.getUser().getId().equals(getCurrentUser().getId())) {
            throw new AccessDeniedException("Brak dostępu do tej transakcji");
        }
        repository.deleteById(id);
    }

    public BalanceDto getUserBalance(Integer days) {
        User user = getCurrentUser();
        List<Transaction> transactions;
        if (days != null && days > 0) {
            LocalDateTime from = LocalDateTime.now().minusDays(days);
            transactions = repository.findAllByUserAndTimestampGreaterThanEqual(user, from);
        } else {
            transactions = repository.findByUser(user);
        }
        double totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
        double totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
        return new BalanceDto(totalIncome, totalExpense, totalIncome - totalExpense);
    }
}

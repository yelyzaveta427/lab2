package com.example.pasir_ihor_kotenko.repository;

import com.example.pasir_ihor_kotenko.model.Transaction;
import com.example.pasir_ihor_kotenko.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(User user);
    List<Transaction> findAllByUserAndTimestampGreaterThanEqual(User user, LocalDateTime timestamp);
}

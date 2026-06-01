package com.example.pasir_ihor_kotenko.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "debts")
public class Debt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Double amount;
    @Column(nullable = false)
    private String title;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debtor_id", nullable = false)
    private User debtor;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creditor_id", nullable = false)
    private User creditor;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false)
    private boolean paidByDebtor = false;
    @Column(nullable = false)
    private boolean confirmedByCreditor = false;
    public Long getId() { return id; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public User getDebtor() { return debtor; }
    public void setDebtor(User debtor) { this.debtor = debtor; }
    public User getCreditor() { return creditor; }
    public void setCreditor(User creditor) { this.creditor = creditor; }
    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isPaidByDebtor() { return paidByDebtor; }
    public void setPaidByDebtor(boolean paidByDebtor) { this.paidByDebtor = paidByDebtor; }
    public boolean isConfirmedByCreditor() { return confirmedByCreditor; }
    public void setConfirmedByCreditor(boolean confirmedByCreditor) { this.confirmedByCreditor = confirmedByCreditor; }
}

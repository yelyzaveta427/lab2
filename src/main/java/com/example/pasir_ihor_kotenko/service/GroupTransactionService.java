package com.example.pasir_ihor_kotenko.service;
import com.example.pasir_ihor_kotenko.dto.GroupTransactionDTO;
import com.example.pasir_ihor_kotenko.dto.TransactionDTO;
import com.example.pasir_ihor_kotenko.model.Debt;
import com.example.pasir_ihor_kotenko.model.Group;
import com.example.pasir_ihor_kotenko.model.Membership;
import com.example.pasir_ihor_kotenko.model.Transaction;
import com.example.pasir_ihor_kotenko.model.User;
import com.example.pasir_ihor_kotenko.repository.DebtRepository;
import com.example.pasir_ihor_kotenko.repository.GroupRepository;
import com.example.pasir_ihor_kotenko.repository.MembershipRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Service
public class GroupTransactionService {
    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final DebtRepository debtRepository;
    private final TransactionService transactionService;
    private final GroupNotificationService groupNotificationService;
    public GroupTransactionService(GroupRepository groupRepository, MembershipRepository membershipRepository, DebtRepository debtRepository, TransactionService transactionService, GroupNotificationService groupNotificationService) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.debtRepository = debtRepository;
        this.transactionService = transactionService;
        this.groupNotificationService = groupNotificationService;
    }
    public Transaction addGroupTransaction(GroupTransactionDTO transactionDTO, User currentUser) {
        Group group = groupRepository.findById(transactionDTO.getGroupId()).orElseThrow(() -> new EntityNotFoundException("Grupa nie istnieje"));
        if (!membershipRepository.existsByGroupIdAndUserId(group.getId(), currentUser.getId())) {
            throw new SecurityException("Użytkownik nie należy do grupy");
        }
        TransactionDTO tx = new TransactionDTO();
        tx.setAmount(transactionDTO.getAmount());
        tx.setType(transactionDTO.getType());
        tx.setTags("group:" + group.getId());
        tx.setNotes(transactionDTO.getTitle());
        Transaction created = transactionService.create(tx);
        List<Membership> members = membershipRepository.findByGroupId(group.getId());
        List<Membership> selectedMembers = selectParticipants(transactionDTO, members, currentUser);
        if (selectedMembers.isEmpty()) {
            throw new IllegalStateException("Grupa nie ma czlonkow, nie mozna dodac transakcji.");
        }
        double amountPerUser = transactionDTO.getAmount() / selectedMembers.size();
        boolean expense = "EXPENSE".equals(transactionDTO.getType());
        for (Membership member : selectedMembers) {
            User otherUser = member.getUser();
            if (!otherUser.getId().equals(currentUser.getId())) {
                Debt debt = new Debt();
                debt.setDebtor(expense ? otherUser : currentUser);
                debt.setCreditor(expense ? currentUser : otherUser);
                debt.setGroup(group);
                debt.setAmount(amountPerUser);
                debt.setTitle(transactionDTO.getTitle());
                debtRepository.save(debt);
            }
        }
        if (expense) {
            groupNotificationService.notifyGroupExpenseAdded(group, currentUser, transactionDTO.getTitle(), transactionDTO.getAmount(), selectedMembers);
        }
        return created;
    }
    private List<Membership> selectParticipants(GroupTransactionDTO transactionDTO, List<Membership> members, User currentUser) {
        List<Long> selectedUserIds = transactionDTO.getSelectedUserIds();
        if (selectedUserIds == null || selectedUserIds.isEmpty()) {
            return members;
        }
        Set<Long> uniqueSelectedUserIds = new HashSet<>(selectedUserIds);
        List<Membership> selectedMembers = members.stream()
                .filter(membership -> uniqueSelectedUserIds.contains(membership.getUser().getId()))
                .toList();
        if (selectedMembers.size() != uniqueSelectedUserIds.size()) {
            throw new IllegalStateException("Wszyscy wybrani uzytkownicy musza byc czlonkami grupy.");
        }
        boolean currentUserSelected = selectedMembers.stream()
                .anyMatch(membership -> membership.getUser().getId().equals(currentUser.getId()));
        if (!currentUserSelected) {
            throw new IllegalStateException("Aktualny uzytkownik musi byc uczestnikiem transakcji grupowej.");
        }
        if (selectedMembers.size() < 2) {
            throw new IllegalStateException("Transakcja grupowa wymaga co najmniej dwoch uczestnikow.");
        }
        return selectedMembers;
    }
}

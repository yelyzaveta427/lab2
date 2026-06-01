package com.example.pasir_ihor_kotenko.service;
import com.example.pasir_ihor_kotenko.dto.DebtDTO;
import com.example.pasir_ihor_kotenko.model.Debt;
import com.example.pasir_ihor_kotenko.model.Group;
import com.example.pasir_ihor_kotenko.model.Membership;
import com.example.pasir_ihor_kotenko.model.User;
import com.example.pasir_ihor_kotenko.repository.DebtRepository;
import com.example.pasir_ihor_kotenko.repository.GroupRepository;
import com.example.pasir_ihor_kotenko.repository.MembershipRepository;
import com.example.pasir_ihor_kotenko.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class DebtService {
    private final DebtRepository debtRepository;
    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final MembershipService membershipService;
    public DebtService(DebtRepository debtRepository, GroupRepository groupRepository, MembershipRepository membershipRepository, UserRepository userRepository, CurrentUserService currentUserService, MembershipService membershipService) {
        this.debtRepository = debtRepository;
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.membershipService = membershipService;
    }
    public List<Debt> getGroupDebts(Long groupId) {
        membershipService.assertCurrentUserIsGroupMember(groupId);
        User current = currentUserService.getCurrentUser();
        Membership membership = membershipRepository.findByGroupIdAndUserId(groupId, current.getId()).orElseThrow(() -> new AccessDeniedException("Użytkownik nie należy do grupy"));
        return debtRepository.findByGroupId(groupId).stream().filter(d -> !d.getCreatedAt().isBefore(membership.getJoinedAt())).toList();
    }
    @Transactional
    public Debt createDebt(DebtDTO dto) {
        Group group = groupRepository.findById(dto.getGroupId()).orElseThrow(() -> new EntityNotFoundException("Grupa nie istnieje"));
        if (dto.getDebtorId().equals(dto.getCreditorId())) { throw new IllegalArgumentException("Nie można utworzyć długu do samego siebie"); }
        User debtor = userRepository.findById(dto.getDebtorId()).orElseThrow(() -> new EntityNotFoundException("Dłużnik nie istnieje"));
        User creditor = userRepository.findById(dto.getCreditorId()).orElseThrow(() -> new EntityNotFoundException("Wierzyciel nie istnieje"));
        membershipService.assertUserIsGroupMember(group.getId(), debtor.getId());
        membershipService.assertUserIsGroupMember(group.getId(), creditor.getId());
        User current = currentUserService.getCurrentUser();
        if (!group.getOwner().getId().equals(current.getId()) && !current.getId().equals(debtor.getId()) && !current.getId().equals(creditor.getId())) {
            throw new AccessDeniedException("Członek grupy może utworzyć dług tylko gdy jest jego uczestnikiem");
        }
        Debt debt = new Debt();
        debt.setGroup(group);
        debt.setDebtor(debtor);
        debt.setCreditor(creditor);
        debt.setAmount(dto.getAmount());
        debt.setTitle(dto.getTitle());
        return debtRepository.findByIdWithAssociations(debtRepository.save(debt).getId()).orElseThrow();
    }
    @Transactional
    public Boolean deleteDebt(Long debtId) {
        Debt debt = debtRepository.findById(debtId).orElseThrow(() -> new EntityNotFoundException("Dług nie istnieje"));
        assertCurrentUserCanManageDebt(debt);
        debtRepository.delete(debt);
        return true;
    }
    public void assertCurrentUserCanManageDebt(Debt debt) {
        User current = currentUserService.getCurrentUser();
        if (debt.getGroup().getOwner().getId().equals(current.getId())) { return; }
        if (debt.getDebtor().getId().equals(current.getId())) { return; }
        if (debt.getCreditor().getId().equals(current.getId())) { return; }
        throw new AccessDeniedException("Brak uprawnień do zarządzania długiem");
    }
    @Transactional
    public Debt markDebtAsPaid(Long debtId) {
        Debt debt = getDebtForCurrentGroupMember(debtId);
        User currentUser = currentUserService.getCurrentUser();
        if (!debt.getDebtor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Tylko dluznik moze oznaczyc dlug jako oplacony.");
        }
        debt.setPaidByDebtor(true);
        debt.setConfirmedByCreditor(false);
        return reloadDebt(debtRepository.save(debt).getId());
    }
    @Transactional
    public Debt confirmDebtPayment(Long debtId) {
        Debt debt = getDebtForCurrentGroupMember(debtId);
        User currentUser = currentUserService.getCurrentUser();
        if (!debt.getCreditor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Tylko wierzyciel moze potwierdzic splate dlugu.");
        }
        if (!debt.isPaidByDebtor()) {
            throw new IllegalStateException("Dlug musi zostac najpierw oznaczony jako oplacony przez dluznika.");
        }
        debt.setConfirmedByCreditor(true);
        return reloadDebt(debtRepository.save(debt).getId());
    }
    private Debt getDebtForCurrentGroupMember(Long debtId) {
        Debt debt = debtRepository.findByIdWithAssociations(debtId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono dlugu o ID " + debtId + "."));
        membershipService.assertCurrentUserIsGroupMember(debt.getGroup().getId());
        return debt;
    }
    private Debt reloadDebt(Long debtId) {
        return debtRepository.findByIdWithAssociations(debtId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono dlugu o ID " + debtId + "."));
    }
}

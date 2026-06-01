package com.example.pasir_ihor_kotenko.service;
import com.example.pasir_ihor_kotenko.dto.MembershipDTO;
import com.example.pasir_ihor_kotenko.dto.MembershipResponseDTO;
import com.example.pasir_ihor_kotenko.model.Group;
import com.example.pasir_ihor_kotenko.model.Membership;
import com.example.pasir_ihor_kotenko.model.User;
import com.example.pasir_ihor_kotenko.repository.GroupRepository;
import com.example.pasir_ihor_kotenko.repository.MembershipRepository;
import com.example.pasir_ihor_kotenko.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class MembershipService {
    private final MembershipRepository membershipRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    public MembershipService(MembershipRepository membershipRepository, GroupRepository groupRepository, UserRepository userRepository, CurrentUserService currentUserService) {
        this.membershipRepository = membershipRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }
    public List<MembershipResponseDTO> getGroupMembers(Long groupId) {
        assertCurrentUserIsGroupMember(groupId);
        return membershipRepository.findByGroupId(groupId).stream().map(m -> new MembershipResponseDTO(m.getId(), m.getGroup().getId(), m.getUser().getId(), m.getUser().getEmail())).toList();
    }
    public MembershipResponseDTO addMember(MembershipDTO dto) {
        assertCurrentUserIsGroupOwner(dto.getGroupId());
        Group group = groupRepository.findById(dto.getGroupId()).orElseThrow(() -> new EntityNotFoundException("Grupa nie istnieje"));
        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow(() -> new EntityNotFoundException("Użytkownik nie istnieje"));
        if (membershipRepository.existsByGroupIdAndUserId(group.getId(), user.getId())) { throw new IllegalArgumentException("Użytkownik już należy do grupy"); }
        Membership membership = new Membership();
        membership.setGroup(group);
        membership.setUser(user);
        membership = membershipRepository.save(membership);
        return new MembershipResponseDTO(membership.getId(), group.getId(), user.getId(), user.getEmail());
    }
    public Boolean removeMember(Long groupId, Long userId) {
        assertCurrentUserIsGroupOwner(groupId);
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("Grupa nie istnieje"));
        if (group.getOwner().getId().equals(userId)) { throw new IllegalArgumentException("Nie można usunąć właściciela grupy"); }
        Membership membership = membershipRepository.findByGroupIdAndUserId(groupId, userId).orElseThrow(() -> new EntityNotFoundException("Członek nie istnieje"));
        membershipRepository.delete(membership);
        return true;
    }
    public void assertCurrentUserIsGroupMember(Long groupId) { assertUserIsGroupMember(groupId, currentUserService.getCurrentUser().getId()); }
    public void assertCurrentUserIsGroupOwner(Long groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("Grupa nie istnieje"));
        if (!group.getOwner().getId().equals(currentUserService.getCurrentUser().getId())) { throw new AccessDeniedException("Tylko właściciel grupy może wykonać tę operację"); }
    }
    public void assertUserIsGroupMember(Long groupId, Long userId) {
        if (!membershipRepository.existsByGroupIdAndUserId(groupId, userId)) { throw new AccessDeniedException("Użytkownik nie należy do grupy"); }
    }
}

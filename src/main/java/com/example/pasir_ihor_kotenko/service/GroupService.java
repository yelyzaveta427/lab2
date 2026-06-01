package com.example.pasir_ihor_kotenko.service;
import com.example.pasir_ihor_kotenko.dto.GroupDTO;
import com.example.pasir_ihor_kotenko.dto.GroupResponseDTO;
import com.example.pasir_ihor_kotenko.model.Group;
import com.example.pasir_ihor_kotenko.model.Membership;
import com.example.pasir_ihor_kotenko.model.User;
import com.example.pasir_ihor_kotenko.repository.DebtRepository;
import com.example.pasir_ihor_kotenko.repository.GroupRepository;
import com.example.pasir_ihor_kotenko.repository.MembershipRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final DebtRepository debtRepository;
    private final CurrentUserService currentUserService;
    public GroupService(GroupRepository groupRepository, MembershipRepository membershipRepository, DebtRepository debtRepository, CurrentUserService currentUserService) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.debtRepository = debtRepository;
        this.currentUserService = currentUserService;
    }
    public List<GroupResponseDTO> groups() {
        return groupRepository.findAll().stream().map(g -> new GroupResponseDTO(g.getId(), g.getName(), g.getOwner().getId())).toList();
    }
    public List<GroupResponseDTO> myGroups() {
        User user = currentUserService.getCurrentUser();
        return groupRepository.findByMemberships_User(user).stream().map(g -> new GroupResponseDTO(g.getId(), g.getName(), g.getOwner().getId())).toList();
    }
    public GroupResponseDTO createGroup(GroupDTO dto) {
        User owner = currentUserService.getCurrentUser();
        Group group = new Group();
        group.setName(dto.getName());
        group.setOwner(owner);
        group = groupRepository.save(group);
        Membership membership = new Membership();
        membership.setGroup(group);
        membership.setUser(owner);
        membershipRepository.save(membership);
        return new GroupResponseDTO(group.getId(), group.getName(), owner.getId());
    }
    @Transactional
    public Boolean deleteGroup(Long id) {
        Group group = groupRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Grupa nie istnieje"));
        if (!group.getOwner().getId().equals(currentUserService.getCurrentUser().getId())) { throw new AccessDeniedException("Tylko właściciel grupy może usunąć grupę"); }
        debtRepository.deleteByGroupId(id);
        membershipRepository.deleteByGroupId(id);
        groupRepository.delete(group);
        return true;
    }
}

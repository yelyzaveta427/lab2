package com.example.pasir_ihor_kotenko.controller;
import com.example.pasir_ihor_kotenko.dto.MembershipDTO;
import com.example.pasir_ihor_kotenko.dto.MembershipResponseDTO;
import com.example.pasir_ihor_kotenko.service.MembershipService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.util.List;
@Controller
public class MembershipGraphQLController {
    private final MembershipService membershipService;
    public MembershipGraphQLController(MembershipService membershipService) { this.membershipService = membershipService; }
    @QueryMapping
    public List<MembershipResponseDTO> groupMembers(@Argument Long groupId) { return membershipService.getGroupMembers(groupId); }
    @MutationMapping
    public MembershipResponseDTO addMember(@Argument @Valid MembershipDTO input) { return membershipService.addMember(input); }
    @MutationMapping
    public Boolean removeMember(@Argument Long groupId, @Argument Long userId) { return membershipService.removeMember(groupId, userId); }
}

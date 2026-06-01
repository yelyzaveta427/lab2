package com.example.pasir_ihor_kotenko.controller;
import com.example.pasir_ihor_kotenko.dto.GroupDTO;
import com.example.pasir_ihor_kotenko.dto.GroupResponseDTO;
import com.example.pasir_ihor_kotenko.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.util.List;
@Controller
public class GroupGraphQLController {
    private final GroupService groupService;
    public GroupGraphQLController(GroupService groupService) { this.groupService = groupService; }
    @QueryMapping
    public List<GroupResponseDTO> groups() { return groupService.groups(); }
    @QueryMapping
    public List<GroupResponseDTO> myGroups() { return groupService.myGroups(); }
    @MutationMapping
    public GroupResponseDTO createGroup(@Argument @Valid GroupDTO input) { return groupService.createGroup(input); }
    @MutationMapping
    public Boolean deleteGroup(@Argument Long id) { return groupService.deleteGroup(id); }
}

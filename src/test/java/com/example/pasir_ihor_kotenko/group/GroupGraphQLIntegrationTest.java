package com.example.pasir_ihor_kotenko.group;
import com.example.pasir_ihor_kotenko.dto.LoginDto;
import com.example.pasir_ihor_kotenko.dto.UserDto;
import com.example.pasir_ihor_kotenko.model.Debt;
import com.example.pasir_ihor_kotenko.model.Group;
import com.example.pasir_ihor_kotenko.model.User;
import com.example.pasir_ihor_kotenko.repository.DebtRepository;
import com.example.pasir_ihor_kotenko.repository.GroupRepository;
import com.example.pasir_ihor_kotenko.repository.MembershipRepository;
import com.example.pasir_ihor_kotenko.repository.TransactionRepository;
import com.example.pasir_ihor_kotenko.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GroupGraphQLIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private MembershipRepository membershipRepository;
    @Autowired
    private DebtRepository debtRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @BeforeEach
    void setUp() {
        debtRepository.deleteAll();
        transactionRepository.deleteAll();
        membershipRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();
    }
    @Test
    @DisplayName("create group and myGroups")
    void createGroupAddsOwnerAsMemberAndReturnsInMyGroups() throws Exception {
        TestUser owner = createAndLoginUser();
        JsonNode createRes = graphql(owner.token, "mutation($input: GroupInput!){createGroup(input:$input){id name ownerId}}", mapOf("input", mapOf("name", "g1")));
        assertFalse(createRes.has("errors"));
        long groupId = createRes.path("data").path("createGroup").path("id").asLong();
        assertTrue(membershipRepository.existsByGroupIdAndUserId(groupId, owner.user.getId()));
        JsonNode myGroupsRes = graphql(owner.token, "query{myGroups{id name ownerId}}", null);
        assertFalse(myGroupsRes.has("errors"));
        assertTrue(containsId(myGroupsRes.path("data").path("myGroups"), groupId));
    }
    @Test
    @DisplayName("only owner adds members")
    void onlyOwnerCanAddMembers() throws Exception {
        TestUser owner = createAndLoginUser();
        TestUser member = createAndLoginUser();
        TestUser outsider = createAndLoginUser();
        long groupId = createGroup(owner.token, "g2");
        JsonNode denied = graphql(outsider.token, "mutation($input: MembershipInput!){addMember(input:$input){id userId email}}", mapOf("input", mapOf("groupId", groupId, "email", member.user.getEmail())));
        assertTrue(denied.has("errors"));
        JsonNode allowed = graphql(owner.token, "mutation($input: MembershipInput!){addMember(input:$input){id userId email}}", mapOf("input", mapOf("groupId", groupId, "email", member.user.getEmail())));
        assertFalse(allowed.has("errors"));
        assertTrue(membershipRepository.existsByGroupIdAndUserId(groupId, member.user.getId()));
    }
    @Test
    @DisplayName("groupMembers only for members")
    void groupMembersAccessibleOnlyToMembers() throws Exception {
        TestUser owner = createAndLoginUser();
        TestUser member = createAndLoginUser();
        TestUser outsider = createAndLoginUser();
        long groupId = createGroup(owner.token, "g3");
        addMember(owner.token, groupId, member.user.getEmail());
        JsonNode memberResult = graphql(member.token, "query($groupId: ID!){groupMembers(groupId:$groupId){id userId email}}", mapOf("groupId", groupId));
        assertFalse(memberResult.has("errors"));
        assertEquals(2, memberResult.path("data").path("groupMembers").size());
        JsonNode outsiderResult = graphql(outsider.token, "query($groupId: ID!){groupMembers(groupId:$groupId){id userId email}}", mapOf("groupId", groupId));
        assertTrue(outsiderResult.has("errors"));
    }
    @Test
    @DisplayName("groupDebts only for members")
    void groupDebtsAccessibleOnlyToMembers() throws Exception {
        TestUser owner = createAndLoginUser();
        TestUser member = createAndLoginUser();
        TestUser outsider = createAndLoginUser();
        long groupId = createGroup(owner.token, "g4");
        addMember(owner.token, groupId, member.user.getEmail());
        createDebt(owner.token, groupId, member.user.getId(), owner.user.getId(), 11.0, "d1");
        JsonNode memberResult = graphql(member.token, "query($groupId: ID!){groupDebts(groupId:$groupId){id title}}", mapOf("groupId", groupId));
        assertFalse(memberResult.has("errors"));
        JsonNode outsiderResult = graphql(outsider.token, "query($groupId: ID!){groupDebts(groupId:$groupId){id title}}", mapOf("groupId", groupId));
        assertTrue(outsiderResult.has("errors"));
    }
    @Test
    @DisplayName("new member sees debts after join")
    void newMemberGetsOnlyDebtsAfterJoin() throws Exception {
        TestUser owner = createAndLoginUser();
        TestUser m1 = createAndLoginUser();
        TestUser m2 = createAndLoginUser();
        long groupId = createGroup(owner.token, "g5");
        addMember(owner.token, groupId, m1.user.getEmail());
        createDebt(owner.token, groupId, m1.user.getId(), owner.user.getId(), 10.0, "before");
        addMember(owner.token, groupId, m2.user.getEmail());
        createDebt(owner.token, groupId, m1.user.getId(), owner.user.getId(), 20.0, "after");
        JsonNode debtsRes = graphql(m2.token, "query($groupId: ID!){groupDebts(groupId:$groupId){id title}}", mapOf("groupId", groupId));
        assertFalse(debtsRes.has("errors"));
        assertFalse(containsTitle(debtsRes.path("data").path("groupDebts"), "before"));
        assertTrue(containsTitle(debtsRes.path("data").path("groupDebts"), "after"));
    }
    @Test
    @DisplayName("income creates debts from current user")
    void incomeGroupTransactionCreatesDebtsFromCurrentUserToOthers() throws Exception {
        TestUser owner = createAndLoginUser();
        TestUser m1 = createAndLoginUser();
        TestUser m2 = createAndLoginUser();
        long groupId = createGroup(owner.token, "g6");
        addMember(owner.token, groupId, m1.user.getEmail());
        addMember(owner.token, groupId, m2.user.getEmail());
        JsonNode txRes = graphql(owner.token, "mutation($input: GroupTransactionInput!){addGroupTransaction(input:$input){id amount type}}", mapOf("input", mapOf("groupId", groupId, "amount", 90.0, "type", "INCOME", "title", "salary")));
        assertFalse(txRes.has("errors"));
        JsonNode debtsRes = graphql(owner.token, "query($groupId: ID!){groupDebts(groupId:$groupId){id debtor{id} creditor{id}}}", mapOf("groupId", groupId));
        assertFalse(debtsRes.has("errors"));
        JsonNode debts = debtsRes.path("data").path("groupDebts");
        assertEquals(2, debts.size());
        assertTrue(allDebtorIs(debts, owner.user.getId().toString()));
    }
    @Test
    @DisplayName("remove member keeps old debts")
    void removingMemberKeepsHistoricalDebts() throws Exception {
        TestUser owner = createAndLoginUser();
        TestUser member = createAndLoginUser();
        long groupId = createGroup(owner.token, "g7");
        addMember(owner.token, groupId, member.user.getEmail());
        long debtId = createDebt(owner.token, groupId, member.user.getId(), owner.user.getId(), 33.0, "hist");
        JsonNode removeRes = graphql(owner.token, "mutation($groupId: ID!, $userId: ID!){removeMember(groupId:$groupId,userId:$userId)}", mapOf("groupId", groupId, "userId", member.user.getId()));
        assertFalse(removeRes.has("errors"));
        assertTrue(removeRes.path("data").path("removeMember").asBoolean());
        JsonNode debtsRes = graphql(owner.token, "query($groupId: ID!){groupDebts(groupId:$groupId){id title}}", mapOf("groupId", groupId));
        assertFalse(debtsRes.has("errors"));
        assertTrue(containsId(debtsRes.path("data").path("groupDebts"), debtId));
    }
    @Test
    @DisplayName("cannot remove owner")
    void cannotRemoveOwnerThroughRemoveMember() throws Exception {
        TestUser owner = createAndLoginUser();
        long groupId = createGroup(owner.token, "g8");
        JsonNode result = graphql(owner.token, "mutation($groupId: ID!, $userId: ID!){removeMember(groupId:$groupId,userId:$userId)}", mapOf("groupId", groupId, "userId", owner.user.getId()));
        assertTrue(result.has("errors"));
    }
    @Test
    @DisplayName("non owner cannot delete group")
    void nonOwnerMemberCannotDeleteGroup() throws Exception {
        TestUser owner = createAndLoginUser();
        TestUser member = createAndLoginUser();
        long groupId = createGroup(owner.token, "g9");
        addMember(owner.token, groupId, member.user.getEmail());
        JsonNode denied = graphql(member.token, "mutation($id: ID!){deleteGroup(id:$id)}", mapOf("id", groupId));
        assertTrue(denied.has("errors"));
        assertTrue(groupRepository.findById(groupId).isPresent());
    }
    @Test
    @DisplayName("createDebt for group members")
    void createDebtOnlyBetweenGroupMembers() throws Exception {
        TestUser owner = createAndLoginUser();
        TestUser member = createAndLoginUser();
        long groupId = createGroup(owner.token, "g10");
        addMember(owner.token, groupId, member.user.getEmail());
        JsonNode ok = graphql(owner.token, "mutation($input: DebtInput!){createDebt(input:$input){id title debtor{id} creditor{id}}}", mapOf("input", mapOf("groupId", groupId, "debtorId", member.user.getId(), "creditorId", owner.user.getId(), "amount", 15.0, "title", "manual")));
        assertFalse(ok.has("errors"));
        assertNotNull(ok.path("data").path("createDebt").path("id").asText(null));
    }
    @Test
    @DisplayName("createDebt rejects outsider and self")
    void createDebtRejectsOutsiderAndSelfDebt() throws Exception {
        TestUser owner = createAndLoginUser();
        TestUser member = createAndLoginUser();
        TestUser outsider = createAndLoginUser();
        long groupId = createGroup(owner.token, "g11");
        addMember(owner.token, groupId, member.user.getEmail());
        JsonNode outsiderDebt = graphql(owner.token, "mutation($input: DebtInput!){createDebt(input:$input){id}}", mapOf("input", mapOf("groupId", groupId, "debtorId", outsider.user.getId(), "creditorId", owner.user.getId(), "amount", 5.0, "title", "outsider")));
        assertTrue(outsiderDebt.has("errors"));
        JsonNode selfDebt = graphql(owner.token, "mutation($input: DebtInput!){createDebt(input:$input){id}}", mapOf("input", mapOf("groupId", groupId, "debtorId", owner.user.getId(), "creditorId", owner.user.getId(), "amount", 5.0, "title", "self")));
        assertTrue(selfDebt.has("errors"));
    }
    @Test
    @DisplayName("owner creates debt between others")
    void ownerCanCreateDebtBetweenOtherMembers() throws Exception {
        TestUser owner = createAndLoginUser();
        TestUser a = createAndLoginUser();
        TestUser b = createAndLoginUser();
        long groupId = createGroup(owner.token, "g12");
        addMember(owner.token, groupId, a.user.getEmail());
        addMember(owner.token, groupId, b.user.getEmail());
        JsonNode result = graphql(owner.token, "mutation($input: DebtInput!){createDebt(input:$input){id debtor{id} creditor{id}}}", mapOf("input", mapOf("groupId", groupId, "debtorId", a.user.getId(), "creditorId", b.user.getId(), "amount", 7.0, "title", "between-others")));
        assertFalse(result.has("errors"));
    }
    @Test
    @DisplayName("member creates debt only with self")
    void memberCanCreateDebtOnlyIfParticipating() throws Exception {
        TestUser owner = createAndLoginUser();
        TestUser a = createAndLoginUser();
        TestUser b = createAndLoginUser();
        TestUser c = createAndLoginUser();
        long groupId = createGroup(owner.token, "g13");
        addMember(owner.token, groupId, a.user.getEmail());
        addMember(owner.token, groupId, b.user.getEmail());
        addMember(owner.token, groupId, c.user.getEmail());
        JsonNode denied = graphql(a.token, "mutation($input: DebtInput!){createDebt(input:$input){id}}", mapOf("input", mapOf("groupId", groupId, "debtorId", b.user.getId(), "creditorId", c.user.getId(), "amount", 12.0, "title", "not-participant")));
        assertTrue(denied.has("errors"));
        JsonNode allowed = graphql(a.token, "mutation($input: DebtInput!){createDebt(input:$input){id}}", mapOf("input", mapOf("groupId", groupId, "debtorId", a.user.getId(), "creditorId", b.user.getId(), "amount", 12.0, "title", "participant")));
        assertFalse(allowed.has("errors"));
    }
    @Test
    @DisplayName("participant can delete debt")
    void deleteDebtDeletesDebtAvailableToParticipant() throws Exception {
        TestUser owner = createAndLoginUser();
        TestUser member = createAndLoginUser();
        long groupId = createGroup(owner.token, "g14");
        addMember(owner.token, groupId, member.user.getEmail());
        long debtId = createDebt(owner.token, groupId, member.user.getId(), owner.user.getId(), 44.0, "del-ok");
        JsonNode deleteRes = graphql(member.token, "mutation($debtId: ID!){deleteDebt(debtId:$debtId)}", mapOf("debtId", debtId));
        assertFalse(deleteRes.has("errors"));
        assertTrue(deleteRes.path("data").path("deleteDebt").asBoolean());
        assertTrue(debtRepository.findById(debtId).isEmpty());
    }
    @Test
    @DisplayName("non participant cannot delete debt")
    void deleteDebtRejectsMemberNotInDebtAndNotOwner() throws Exception {
        TestUser owner = createAndLoginUser();
        TestUser a = createAndLoginUser();
        TestUser b = createAndLoginUser();
        long groupId = createGroup(owner.token, "g15");
        addMember(owner.token, groupId, a.user.getEmail());
        addMember(owner.token, groupId, b.user.getEmail());
        long debtId = createDebt(owner.token, groupId, owner.user.getId(), a.user.getId(), 25.0, "restricted");
        JsonNode denied = graphql(b.token, "mutation($debtId: ID!){deleteDebt(debtId:$debtId)}", mapOf("debtId", debtId));
        assertTrue(denied.has("errors"));
        assertTrue(debtRepository.findById(debtId).isPresent());
    }
    @Test
    @DisplayName("owner deletes any group debt")
    void ownerCanDeleteDebtNotParticipatingIn() throws Exception {
        TestUser owner = createAndLoginUser();
        TestUser a = createAndLoginUser();
        TestUser b = createAndLoginUser();
        long groupId = createGroup(owner.token, "g16");
        addMember(owner.token, groupId, a.user.getEmail());
        addMember(owner.token, groupId, b.user.getEmail());
        long debtId = createDebt(owner.token, groupId, a.user.getId(), b.user.getId(), 18.0, "owner-delete");
        JsonNode deleted = graphql(owner.token, "mutation($debtId: ID!){deleteDebt(debtId:$debtId)}", mapOf("debtId", debtId));
        assertFalse(deleted.has("errors"));
        assertTrue(deleted.path("data").path("deleteDebt").asBoolean());
    }
    @Test
    @DisplayName("graphql input validation")
    void graphqlInputValidationRejectsInvalidValues() throws Exception {
        TestUser owner = createAndLoginUser();
        long groupId = createGroup(owner.token, "g17");
        JsonNode invalidGroup = graphql(owner.token, "mutation($input: GroupInput!){createGroup(input:$input){id}}", mapOf("input", mapOf("name", "")));
        assertTrue(invalidGroup.has("errors"));
        JsonNode invalidMember = graphql(owner.token, "mutation($input: MembershipInput!){addMember(input:$input){id}}", mapOf("input", mapOf("groupId", groupId, "email", "bad-email")));
        assertTrue(invalidMember.has("errors"));
        JsonNode invalidDebt = graphql(owner.token, "mutation($input: DebtInput!){createDebt(input:$input){id}}", mapOf("input", mapOf("groupId", groupId, "debtorId", owner.user.getId(), "creditorId", owner.user.getId(), "amount", 0.0, "title", "")));
        assertTrue(invalidDebt.has("errors"));
    }
    @Test
    @DisplayName("owner deletes group and debts")
    void deletingGroupByOwnerDeletesDebtsAndGroup() throws Exception {
        TestUser owner = createAndLoginUser();
        TestUser member = createAndLoginUser();
        long groupId = createGroup(owner.token, "g18");
        addMember(owner.token, groupId, member.user.getEmail());
        createDebt(owner.token, groupId, member.user.getId(), owner.user.getId(), 29.0, "cleanup");
        JsonNode deleteGroupRes = graphql(owner.token, "mutation($id: ID!){deleteGroup(id:$id)}", mapOf("id", groupId));
        assertFalse(deleteGroupRes.has("errors"));
        assertTrue(deleteGroupRes.path("data").path("deleteGroup").asBoolean());
        assertTrue(groupRepository.findById(groupId).isEmpty());
        assertEquals(0, debtRepository.findByGroupId(groupId).size());
    }
    private long createGroup(String token, String name) throws Exception {
        JsonNode res = graphql(token, "mutation($input: GroupInput!){createGroup(input:$input){id}}", mapOf("input", mapOf("name", name)));
        return res.path("data").path("createGroup").path("id").asLong();
    }
    private void addMember(String token, long groupId, String email) throws Exception {
        JsonNode res = graphql(token, "mutation($input: MembershipInput!){addMember(input:$input){id}}", mapOf("input", mapOf("groupId", groupId, "email", email)));
        assertFalse(res.has("errors"));
    }
    private long createDebt(String token, long groupId, long debtorId, long creditorId, double amount, String title) throws Exception {
        JsonNode res = graphql(token, "mutation($input: DebtInput!){createDebt(input:$input){id}}", mapOf("input", mapOf("groupId", groupId, "debtorId", debtorId, "creditorId", creditorId, "amount", amount, "title", title)));
        assertFalse(res.has("errors"));
        return res.path("data").path("createDebt").path("id").asLong();
    }
    private TestUser createAndLoginUser() throws Exception {
        String email = "u_" + UUID.randomUUID().toString().substring(0, 8) + "@pk.pl";
        String password = "SecurePass123";
        UserDto userDto = new UserDto();
        userDto.setEmail(email);
        userDto.setPassword(password);
        userDto.setCurrency("PLN");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(userDto))).andExpect(status().isOk());
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail(email);
        loginDto.setPassword(password);
        MvcResult loginRes = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginDto))).andExpect(status().isOk()).andReturn();
        String token = objectMapper.readTree(loginRes.getResponse().getContentAsString()).path("token").asText();
        User user = userRepository.findByEmail(email).orElseThrow();
        return new TestUser(user, token);
    }
    private JsonNode graphql(String token, String query, Map<String, Object> variables) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("query", query);
        if (variables != null) {
            body.put("variables", variables);
        }
        MvcResult res = mockMvc.perform(post("/graphql").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body))).andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString());
    }
    private Map<String, Object> mapOf(Object... pairs) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String) pairs[i], pairs[i + 1]);
        }
        return map;
    }
    private boolean containsId(JsonNode array, long id) {
        for (JsonNode node : array) {
            if (node.path("id").asLong() == id) {
                return true;
            }
        }
        return false;
    }
    private boolean containsTitle(JsonNode array, String title) {
        for (JsonNode node : array) {
            if (title.equals(node.path("title").asText())) {
                return true;
            }
        }
        return false;
    }
    private boolean allDebtorIs(JsonNode array, String debtorId) {
        for (JsonNode node : array) {
            if (!debtorId.equals(node.path("debtor").path("id").asText())) {
                return false;
            }
        }
        return true;
    }
    private record TestUser(User user, String token) {}
}

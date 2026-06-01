import graphqlClient from "./graphClient";
export type Id = number | string;
interface Group {
  id: Id;
  name: string;
  ownerId: Id;
}
interface GroupMemberRaw {
  id: Id;
  userId: Id;
  groupId: Id;
  email: string;
}
interface GroupMember {
  id: Id;
  userId: Id;
  groupId: Id;
  userEmail: string;
}
export interface GroupDebt {
  id: Id;
  title: string;
  amount: number;
  paidByDebtor: boolean;
  confirmedByCreditor: boolean;
  debtor: { id: Id; email: string };
  creditor: { id: Id; email: string };
}
const mapMember = (member: GroupMemberRaw): GroupMember => ({
  id: member.id,
  userId: member.userId,
  groupId: member.groupId,
  userEmail: member.email,
});
const isGroupDebtsNullError = (error: unknown) =>
  error instanceof Error &&
  error.message.includes("field at path '/groupDebts'") &&
  error.message.includes("non null type");
export const groupsApi = {
  getGroups: async () => {
    const query = `
      query {
        myGroups {
          id
          name
          ownerId
        }
      }
    `;
    const response = await graphqlClient<{ myGroups: Group[] }>(query);
    return response.data.myGroups;
  },
  createGroup: async (name: string) => {
    const mutation = `
      mutation CreateGroup($input: GroupInput!) {
        createGroup(input: $input) {
          id
          name
          ownerId
        }
      }
    `;
    const response = await graphqlClient<{ createGroup: Group }>(mutation, {
      input: { name },
    });
    return response.data.createGroup;
  },
  getGroupMembers: async (groupId: Id) => {
    const query = `
      query($groupId: ID!) {
        groupMembers(groupId: $groupId) {
          id
          userId
          groupId
          email
        }
      }
    `;
    const response = await graphqlClient<{ groupMembers: GroupMemberRaw[] }>(
      query,
      { groupId }
    );
    return response.data.groupMembers.map(mapMember);
  },
  addMember: async (groupId: Id, userEmail: string) => {
    const mutation = `
      mutation($input: MembershipInput!) {
        addMember(input: $input) {
          id
          groupId
          userId
        }
      }
    `;
    return graphqlClient(mutation, { input: { groupId, email: userEmail } });
  },
  deleteGroup: async (id: Id) => {
    const mutation = `
      mutation($id: ID!) {
        deleteGroup(id: $id)
      }
    `;
    return graphqlClient(mutation, { id });
  },
  removeMember: async (groupId: Id, userId: Id) => {
    const mutation = `
      mutation($groupId: ID!, $userId: ID!) {
        removeMember(groupId: $groupId, userId: $userId)
      }
    `;
    return graphqlClient(mutation, { groupId, userId });
  },
  addGroupTransaction: async (
    groupId: Id,
    amount: number,
    type: string,
    title: string,
    selectedUserIds?: Id[]
  ) => {
    const mutation = `
      mutation($input: GroupTransactionInput!) {
        addGroupTransaction(input: $input) {
          id
          amount
          type
        }
      }
    `;
    const input: Record<string, unknown> = { groupId, amount, type, title };
    if (selectedUserIds && selectedUserIds.length > 0) {
      input.selectedUserIds = selectedUserIds;
    }
    return graphqlClient(mutation, { input });
  },
  createDebt: async (
    groupId: Id,
    debtorId: Id,
    creditorId: Id,
    amount: number,
    title: string
  ) => {
    const mutation = `
      mutation($input: DebtInput!) {
        createDebt(input: $input) {
          id
          title
          amount
          paidByDebtor
          confirmedByCreditor
          debtor {
            id
            email
          }
          creditor {
            id
            email
          }
        }
      }
    `;
    const response = await graphqlClient<{ createDebt: GroupDebt }>(mutation, {
      input: { groupId, debtorId, creditorId, amount, title },
    });
    return response.data.createDebt;
  },
  deleteDebt: async (debtId: Id) => {
    const mutation = `
      mutation($debtId: ID!) {
        deleteDebt(debtId: $debtId)
      }
    `;
    return graphqlClient(mutation, { debtId });
  },
  getDebts: async (groupId: Id): Promise<GroupDebt[]> => {
    const query = `
      query($groupId: ID!) {
        groupDebts(groupId: $groupId) {
          id
          title
          amount
          paidByDebtor
          confirmedByCreditor
          debtor {
            id
            email
          }
          creditor {
            id
            email
          }
        }
      }
    `;
    try {
      const response = await graphqlClient<{ groupDebts: GroupDebt[] | null }>(
        query,
        { groupId }
      );
      return response.data.groupDebts ?? [];
    } catch (error) {
      if (isGroupDebtsNullError(error)) {
        return [];
      }
      throw error;
    }
  },
  markDebtAsPaid: async (debtId: Id) => {
    const mutation = `
      mutation($debtId: ID!) {
        markDebtAsPaid(debtId: $debtId) {
          id
          title
          amount
          paidByDebtor
          confirmedByCreditor
          debtor {
            id
            email
          }
          creditor {
            id
            email
          }
        }
      }
    `;
    const response = await graphqlClient<{ markDebtAsPaid: GroupDebt }>(
      mutation,
      { debtId }
    );
    return response.data.markDebtAsPaid;
  },
  confirmDebtPayment: async (debtId: Id) => {
    const mutation = `
      mutation($debtId: ID!) {
        confirmDebtPayment(debtId: $debtId) {
          id
          title
          amount
          paidByDebtor
          confirmedByCreditor
          debtor {
            id
            email
          }
          creditor {
            id
            email
          }
        }
      }
    `;
    const response = await graphqlClient<{ confirmDebtPayment: GroupDebt }>(
      mutation,
      { debtId }
    );
    return response.data.confirmDebtPayment;
  },
};

import type { User } from "../context/AuthContext";
type Id = number | string;
interface GroupLike {
  ownerId: Id;
}
interface MemberLike {
  userId: Id;
  userEmail: string;
}
export function isGroupOwner(
  user: User | null | undefined,
  group: GroupLike,
  members: MemberLike[] = []
): boolean {
  if (!user?.email) return false;
  if (user.id !== undefined && String(user.id) === String(group.ownerId)) {
    return true;
  }
  return members.some(
    (m) =>
      m.userEmail === user.email &&
      String(m.userId) === String(group.ownerId)
  );
}

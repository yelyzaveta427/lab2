package com.example.pasir_ihor_kotenko.repository;
import com.example.pasir_ihor_kotenko.model.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    @Query("select m from Membership m join fetch m.user join fetch m.group where m.group.id = :groupId")
    List<Membership> findByGroupId(@Param("groupId") Long groupId);
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    void deleteByGroupId(Long groupId);
    @Query("select m from Membership m join fetch m.user join fetch m.group where m.group.id = :groupId and m.user.id = :userId")
    Optional<Membership> findByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);
}

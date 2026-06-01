package com.example.pasir_ihor_kotenko.repository;
import com.example.pasir_ihor_kotenko.model.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface DebtRepository extends JpaRepository<Debt, Long> {
    @Query("select d from Debt d join fetch d.debtor join fetch d.creditor join fetch d.group g join fetch g.owner where g.id = :groupId")
    List<Debt> findByGroupId(@Param("groupId") Long groupId);
    @Query("select d from Debt d join fetch d.debtor join fetch d.creditor join fetch d.group g join fetch g.owner where d.id = :id")
    Optional<Debt> findByIdWithAssociations(@Param("id") Long id);
    void deleteByGroupId(Long groupId);
}

package com.example.pasir_ihor_kotenko.repository;
import com.example.pasir_ihor_kotenko.model.Group;
import com.example.pasir_ihor_kotenko.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByMemberships_User(User user);
}

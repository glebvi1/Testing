package testing_system.repos.people;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testing_system.domain.people.Roles;
import testing_system.domain.people.User;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);

    List<User> findAllByUsernameAndFullName(String username, String fullName);

    List<User> findAllByFullName(String fullName);

    User findByActivatedCode(String code);

    Page<User> findAll(Pageable pageable);
}

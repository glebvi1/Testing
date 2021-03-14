package testing_system.repos.people;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testing_system.domain.people.User;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);

    List<User> findAllByUsernameAndFullName(String username, String fullName);

    List<User> findAllByFullName(String fullName);

    User findByActivatedCode(String code);
}

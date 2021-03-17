package testing_system.repos.people;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testing_system.domain.people.Users;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<Users, Long> {
    Users findByUsername(String username);

    List<Users> findAllByUsernameAndFullName(String username, String fullName);

    List<Users> findAllByFullName(String fullName);

    Users findByActivatedCode(String code);

    Page<Users> findAll(Pageable pageable);
}

package testing_system.repos.people;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testing_system.domain.people.Student;

@Repository
public interface StudentRepo extends JpaRepository<Student, Long> {
    Student findByUsername(String username);
}

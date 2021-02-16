package testing_system.repos.people;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testing_system.domain.people.Teacher;

@Repository
public interface TeacherRepo extends JpaRepository<Teacher, Long> {
    Teacher findByUsername(String username);
}

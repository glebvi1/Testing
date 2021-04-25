package testing_system.repos.test;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testing_system.domain.test.StudentsAnswers;

@Repository
public interface StudentsAnswersRepo extends JpaRepository<StudentsAnswers, Long> {
}

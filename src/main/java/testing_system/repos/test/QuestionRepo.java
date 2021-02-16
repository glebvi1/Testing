package testing_system.repos.test;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testing_system.domain.test.Question;

@Repository
public interface QuestionRepo extends JpaRepository<Question, Long> {
}

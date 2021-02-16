package testing_system.repos.test;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testing_system.domain.test.Test;

@Repository
public interface TestRepo extends JpaRepository<Test, Long> {
    Test findTestByTitle(String title);
}

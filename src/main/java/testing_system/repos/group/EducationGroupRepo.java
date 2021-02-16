package testing_system.repos.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testing_system.domain.group.EducationGroup;

@Repository
public interface EducationGroupRepo extends JpaRepository<EducationGroup, Long> {
    EducationGroup findByTitle(String title);
}

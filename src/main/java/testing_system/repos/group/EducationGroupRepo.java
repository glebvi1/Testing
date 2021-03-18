package testing_system.repos.group;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testing_system.domain.group.EducationGroup;

import java.util.List;

@Repository
public interface EducationGroupRepo extends JpaRepository<EducationGroup, Long> {
    EducationGroup findByTitle(String title);

    Page<EducationGroup> findAll(Pageable pageable);
}

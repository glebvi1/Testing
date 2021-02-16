package testing_system.repos.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testing_system.domain.group.Module;

@Repository
public interface ModuleRepo extends JpaRepository<Module, Long> {
}

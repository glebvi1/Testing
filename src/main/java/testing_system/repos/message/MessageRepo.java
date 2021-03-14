package testing_system.repos.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testing_system.domain.message.Message;

@Repository
public interface MessageRepo extends JpaRepository<Message, Long> {

}

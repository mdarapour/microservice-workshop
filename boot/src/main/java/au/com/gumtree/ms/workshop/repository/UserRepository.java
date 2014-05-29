package au.com.gumtree.ms.workshop.repository;

import au.com.gumtree.ms.workshop.domain.User;
import org.springframework.data.repository.CrudRepository;

/**
 * @author mdarapour
 */
public interface UserRepository extends CrudRepository<User, Long> {
}

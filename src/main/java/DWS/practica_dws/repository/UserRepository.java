package DWS.practica_dws.repository;

import java.util.Optional;

import DWS.practica_dws.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String name);

}


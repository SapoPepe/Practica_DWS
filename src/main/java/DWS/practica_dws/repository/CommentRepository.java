package DWS.practica_dws.repository;

import DWS.practica_dws.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByCIDAndUserName(long CID, String userName);
    Optional<Comment> findByUserName(String userName);

}

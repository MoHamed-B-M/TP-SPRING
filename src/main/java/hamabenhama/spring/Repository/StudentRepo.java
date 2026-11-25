package hamabenhama.spring.Repository;

import hamabenhama.spring.Entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepo extends JpaRepository<StudentEntity, Long> {

    List<StudentEntity> findByLevel(String level);

    List<StudentEntity> findByNameContainingIgnoreCase(String name);
}

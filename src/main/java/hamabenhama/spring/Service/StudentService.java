package hamabenhama.spring.Service;

import hamabenhama.spring.Entity.StudentEntity;
import hamabenhama.spring.Repository.StudentRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepo studentRepo;

    public StudentService(StudentRepo studentRepo) {
        this.studentRepo = studentRepo;
    }

    public List<StudentEntity> getAllStudents() {
        return studentRepo.findAll();
    }

    public StudentEntity getStudentById(Long id) {
        return studentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
    }

    public StudentEntity createStudent(StudentEntity student) {
        return studentRepo.save(student);
    }

    public StudentEntity updateStudent(Long id, StudentEntity updated) {
        StudentEntity existing = getStudentById(id);
        existing.setName(updated.getName());
        existing.setLevel(updated.getLevel());
        return studentRepo.save(existing);
    }

    public void deleteStudent(Long id) {
        if (!studentRepo.existsById(id)) {
            throw new RuntimeException("Student not found with id: " + id);
        }
        studentRepo.deleteById(id);
    }

    public List<StudentEntity> getStudentsByLevel(String level) {
        return studentRepo.findByLevel(level);
    }
}

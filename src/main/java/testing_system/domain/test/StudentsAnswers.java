package testing_system.domain.test;

import testing_system.domain.people.Student;

import javax.persistence.*;
import java.util.List;

@Table
@Entity
public class StudentsAnswers {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    private Student student;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> studentsAnswers;

    private boolean isRight;

    public StudentsAnswers() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public List<String> getStudentsAnswers() {
        return studentsAnswers;
    }

    public void setStudentsAnswers(List<String> studentsAnswers) {
        this.studentsAnswers = studentsAnswers;
    }

    public boolean isRight() {
        return isRight;
    }

    public void setRight(boolean isRight) {
        this.isRight = isRight;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StudentsAnswers) {
            return id == ((StudentsAnswers) obj).getId();
        }
        return false;
    }
}

package testing_system.domain.test;

import testing_system.domain.group.Module;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

@Entity
@Table
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String title;
    private int sections;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Question> questions;

    // Key (long) - student's id, value (mark) - student's mark
    // Оценки студентов
    @ElementCollection(fetch = FetchType.EAGER)
    private Map<Long, Integer> studentsMarks;

    // Система оценивания (5 - 100%, 4 - 75% и т д)
    @ElementCollection(fetch = FetchType.LAZY)
    private Map<Integer, Integer> gradingSystem;

    @ManyToOne(fetch = FetchType.LAZY)
    private Module module;

    public Test() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public Map<Long, Integer> getStudentsMarks() {
        return studentsMarks;
    }

    public void setStudentsMarks(Map<Long, Integer> studentsMarks) {
        this.studentsMarks = studentsMarks;
    }

    public Map<Integer, Integer> getGradingSystem() {
        return gradingSystem;
    }

    public void setGradingSystem(Map<Integer, Integer> gradingSystem) {
        this.gradingSystem = gradingSystem;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public int getSections() {
        return sections;
    }

    public void setSections(int sections) {
        this.sections = sections;
    }

}

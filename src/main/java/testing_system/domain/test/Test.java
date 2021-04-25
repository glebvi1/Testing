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

    // Прикрепленный файл от учителя
    private String filename;

    // Тип теста: с файлами или без
    private boolean isFile;

    // true - Если тест создан из КР
    private boolean isControl;

    // Long - id студента, String - файлы, идущие через пробел
    // Файлы, прикрепленные студентом
    @ElementCollection(fetch = FetchType.LAZY)
    private Map<Long, String> studentsSolving;

    // Название разделов. Только для КР
    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> sectionTitle;

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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Map<Long, String> getStudentsSolving() {
        return studentsSolving;
    }

    public void setStudentsSolving(Map<Long, String> studentsSolving) {
        this.studentsSolving = studentsSolving;
    }

    public boolean isIsFile() {
        return isFile;
    }

    public void setIsFile(boolean type) {
        this.isFile = type;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public boolean isControl() {
        return isControl;
    }

    public void setControl(boolean control) {
        isControl = control;
    }

    public List<String> getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(List<String> sectionTitle) {
        this.sectionTitle = sectionTitle;
    }
}

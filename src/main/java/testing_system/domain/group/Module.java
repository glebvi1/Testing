package testing_system.domain.group;

import testing_system.domain.test.Test;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String title;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("title asc")
    private Set<Test> tests;

    @ManyToOne(fetch = FetchType.LAZY)
    private EducationGroup educationGroup;

    public Module() {}

    public Module(String title) {
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Set<Test> getTests() {
        return tests;
    }

    public void setTests(Set<Test> tests) {
        this.tests = tests;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public EducationGroup getEducationGroup() {
        return educationGroup;
    }

    public void setEducationGroup(EducationGroup educationGroup) {
        this.educationGroup = educationGroup;
    }
}

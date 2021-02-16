package testing_system.domain.people;

import testing_system.domain.group.EducationGroup;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

@Entity
@Table
public class Student extends User {

    @ManyToMany(fetch = FetchType.EAGER)
    private List<EducationGroup> groups;

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<Long, Integer> allMarks; // key (long) - test's id. value (int) - mark

    public Student() {}

    public List<EducationGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<EducationGroup> groups) {
        this.groups = groups;
    }

    public Map<Long, Integer> getAllMarks() {
        return allMarks;
    }

    public void setAllMarks(Map<Long, Integer> allMarks) {
        this.allMarks = allMarks;
    }

}

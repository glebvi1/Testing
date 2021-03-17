package testing_system.domain.people;

import testing_system.domain.group.EducationGroup;

import javax.persistence.*;
import java.util.List;

@Entity
@Table
public class Teacher extends Users {

    @ManyToMany(fetch = FetchType.EAGER)
    private List<EducationGroup> groups;

    public Teacher() {}

    public Teacher(String username) {
        super(username);
    }

    public List<EducationGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<EducationGroup> groups) {
        this.groups = groups;
    }

}

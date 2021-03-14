package testing_system.service;

import org.springframework.stereotype.Service;
import testing_system.domain.group.EducationGroup;
import testing_system.domain.people.Roles;
import testing_system.domain.people.User;

import java.util.Set;

@Service
public class AuxiliaryService {

    public boolean security(User user, EducationGroup educationGroup) {

        for (User u : educationGroup.getStudents()) {
            if (u.getId() == user.getId()) {
                return true;
            }
        }
        for (User u : educationGroup.getTeachers()) {
            if (u.getId() == user.getId()) {
                return true;
            }
        }

        return user.getRoles().contains(Roles.TEACHER_ADMIN);
    }

    public String getRole(User user) {
        Set<Roles> roles = user.getRoles();
        if (roles.contains(Roles.SYSTEM_ADMIN)) {
            return "system_admin";
        }
        if (roles.contains(Roles.TEACHER_ADMIN)) {
            return "teacher_admin";
        }
        if (roles.contains(Roles.TEACHER)) {
            return "teacher";
        }
        if (roles.contains(Roles.STUDENT)) {
            return "student";
        }
        return "";
    }

}

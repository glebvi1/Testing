package testing_system.service;

import org.springframework.stereotype.Service;
import testing_system.domain.group.EducationGroup;
import testing_system.domain.people.Users;
import testing_system.domain.people.Roles;

import java.util.Set;

@Service
public class AuxiliaryService {

    // Защита от перемещения учителей в группы, в которых они не ведут занятия
    public boolean security(Users user, EducationGroup educationGroup) {

        for (Users u : educationGroup.getStudents()) {
            if (u.getId() == user.getId()) {
                return true;
            }
        }
        for (Users u : educationGroup.getTeachers()) {
            if (u.getId() == user.getId()) {
                return true;
            }
        }

        return user.getRoles().contains(Roles.TEACHER_ADMIN);
    }

    // Верхняя роль пользователя
    public String getRole(Users user) {
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

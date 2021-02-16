package testing_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import testing_system.domain.group.EducationGroup;
import testing_system.domain.people.Roles;
import testing_system.domain.people.Student;
import testing_system.domain.people.Teacher;
import testing_system.domain.people.User;
import testing_system.repos.group.EducationGroupRepo;
import testing_system.repos.people.StudentRepo;
import testing_system.repos.people.TeacherRepo;
import testing_system.repos.people.UserRepo;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SystemAdminService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private TeacherRepo teacherRepo;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private EducationGroupRepo educationGroupRepo;

    public boolean editUser(User user, Map<String, String> htmlRoles) {
        if (htmlRoles.isEmpty()) {
            return false;
        }

        Set<String> stringRoles = Arrays.stream(Roles.values())
                .map(Roles::name)
                .collect(Collectors.toSet());

        int oldRole = 0;
        if (user.getRoles().contains(Roles.STUDENT)) {
            oldRole = 1;
        } else if (user.getRoles().contains(Roles.TEACHER)) {
            oldRole = 2;
        }

        user.getRoles().clear();

        for (String key : htmlRoles.keySet()) {
            if (stringRoles.contains(key)) {
                user.getRoles().add(Roles.valueOf(key));
            }
        }

        Set<Roles> roles = user.getRoles();

        if (roles.contains(Roles.TEACHER_ADMIN) || roles.contains(Roles.SYSTEM_ADMIN)) {
            User newUser = new User();
            newUser.setUsername(user.getUsername());
            newUser.setPassword(user.getPassword());
            newUser.setFullName(user.getFullName());
            newUser.setRoles(roles);

            if (oldRole == 1) {
                studentRepo.deleteById(user.getId());
            } else if (oldRole == 2) {
                teacherRepo.deleteById(user.getId());
            }
            userRepo.save(newUser);

        } else if (roles.contains(Roles.TEACHER)) {
            Teacher teacher = new Teacher();
            teacher.setRoles(roles);
            teacher.setPassword(user.getPassword());
            teacher.setUsername(user.getUsername());
            teacher.setFullName(user.getFullName());

            userRepo.delete(user);
            teacherRepo.save(teacher);

        } else if (roles.contains(Roles.STUDENT)) {
            Student student = new Student();
            student.setRoles(roles);
            student.setPassword(user.getPassword());
            student.setUsername(user.getUsername());
            student.setFullName(user.getFullName());

            userRepo.delete(user);
            studentRepo.save(student);
        }
        return true;
    }

    public List<User> sort(String username, String fullName) {
        List<User> users = new ArrayList<>();

        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(fullName)) {
            users = userRepo.findAllByUsernameAndFullName(username, fullName);
        } else if (!StringUtils.isEmpty(username)) {
            users.add(userRepo.findByUsername(username));
        } else if (!StringUtils.isEmpty(fullName)) {
            users = userRepo.findAllByFullName(fullName);
        }

        users.removeIf(user -> (user.getRoles().contains(Roles.SYSTEM_ADMIN) || user.getRoles().contains(Roles.TEACHER_ADMIN)));
        return users;
    }

    public boolean createEducationGroup(List<String> studentsEmails, List<String> teachersEmails,
                                     String title) {

        if (StringUtils.isEmpty(title) || educationGroupRepo.findByTitle(title) != null) {
            return false;
        }

        List<Student> students = new ArrayList<>();

        // Добавляем студентов в список
        for (String email : studentsEmails) {
            if (StringUtils.isEmpty(email)) {
                continue;
            }
            Student student = studentRepo.findByUsername(email);
            if (student == null) {
                // TODO: 1. сохранять форму; указать на ошибочную почту 2. сделать ф-ию editEducationGroup
                continue;
            }
            students.add(student);
        }

        if (students.size() == 0) {
            return false;
        }

        List<Teacher> teachers = new ArrayList<>();

        // Добавляем учителей в список
        for (String email : teachersEmails) {
            if (StringUtils.isEmpty(email)) {
                continue;
            }
            Teacher teacher = teacherRepo.findByUsername(email);
            if (teacher == null) {
                // TODO: 1. сохранять форму; указать на ошибочную почту 2. сделать ф-ию editEducationGroup
                continue;
            }
            teachers.add(teacher);
        }

        if (teachers.size() == 0) {
            return false;
        }

        // Создаем группу
        EducationGroup educationGroup = new EducationGroup();
        educationGroup.setTeachers(teachers);
        educationGroup.setTitle(title);
        educationGroup.setStudents(students);
        educationGroupRepo.save(educationGroup);


        // Добавляем группу к пользователям
        for (Student student : students) {
            student.getGroups().add(educationGroup);
            studentRepo.save(student);
        }
        for (Teacher teacher : teachers) {
            teacher.getGroups().add(educationGroup);
            teacherRepo.save(teacher);
        }

        return true;
    }

}

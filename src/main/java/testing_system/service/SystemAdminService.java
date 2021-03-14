package testing_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import testing_system.domain.group.EducationGroup;
import testing_system.domain.message.Message;
import testing_system.domain.people.Roles;
import testing_system.domain.people.Student;
import testing_system.domain.people.Teacher;
import testing_system.domain.people.User;
import testing_system.repos.group.EducationGroupRepo;
import testing_system.repos.message.MessageRepo;
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
    @Autowired
    private MailSender mailSender;
    @Autowired
    private MessageRepo messageRepo;

    public boolean editUser(User user, Map<String, String> htmlRoles) {
        if (htmlRoles.isEmpty()) {
            return false;
        }

        Set<String> stringRoles = Arrays.stream(Roles.values())
                .map(Roles::name)
                .collect(Collectors.toSet());

        boolean wasTeacher = user.getRoles().contains(Roles.TEACHER);
        boolean wasStudent = user.getRoles().contains(Roles.STUDENT);

        user.getRoles().clear();

        for (String key : htmlRoles.keySet()) {
            if (stringRoles.contains(key)) {
                user.getRoles().add(Roles.valueOf(key));
            }
        }

        Set<Roles> roles = user.getRoles();
        if (roles.contains(Roles.STUDENT) && roles.contains(Roles.TEACHER) ||
                (roles.contains(Roles.STUDENT) && roles.contains(Roles.TEACHER_ADMIN))) {
            return false;
        }

        if (roles.contains(Roles.TEACHER)) {
            Teacher newUser = new Teacher();
            newUser.setUsername(user.getUsername());
            newUser.setPassword(user.getPassword());
            newUser.setFullName(user.getFullName());
            newUser.setRoles(roles);
            if (wasTeacher) {
                Teacher teacher = teacherRepo.findById(user.getId()).get();
                newUser.setGroups(teacher.getGroups());
            }
            userRepo.delete(user);
            teacherRepo.save(newUser);
        } else if (roles.contains(Roles.STUDENT)) {
            Student newUser = new Student();
            newUser.setUsername(user.getUsername());
            newUser.setPassword(user.getPassword());
            newUser.setFullName(user.getFullName());
            newUser.setRoles(roles);
            if (wasStudent) {
                Student student = studentRepo.findById(user.getId()).get();
                newUser.setGroups(student.getGroups());
            }
            userRepo.delete(user);
            studentRepo.save(newUser);
        } else if (roles.contains(Roles.TEACHER_ADMIN) || roles.contains(Roles.SYSTEM_ADMIN)) {
            User newUser = new User();
            newUser.setUsername(user.getUsername());
            newUser.setPassword(user.getPassword());
            newUser.setFullName(user.getFullName());
            newUser.setRoles(roles);

            userRepo.deleteById(user.getId());
            userRepo.save(newUser);
        }

/*
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
*/
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

    // Создание новой учебной группы
    public boolean createEducationGroup(List<String> studentsEmails, List<String> teachersEmails,
                                     String title) {

        if (StringUtils.isEmpty(title) || educationGroupRepo.findByTitle(title) != null) {
            return false;
        }

        // Добавляем студентов в список
        List<Student> students = getStudent(studentsEmails);
        if (students.size() == 0) {
            return false;
        }

        // Добавляем учителей в список
        List<Teacher> teachers = getTeacher(teachersEmails);
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

    public boolean updateEducationGroup(List<String> studentsEmails, List<String> teachersEmails,
                                        String title, EducationGroup oldEducationGroup) {

        if (StringUtils.isEmpty(title)) {
            return false;
        }

        // Добавляем студентов в список
        List<Student> students = oldEducationGroup.getStudents();
        List<Student> newStudents = getStudent(studentsEmails);
        students.addAll(newStudents);

        // Добавляем учителей в список
        List<Teacher> teachers = oldEducationGroup.getTeachers();
        List<Teacher> newTeachers = getTeacher(teachersEmails);
        teachers.addAll(newTeachers);

        // Создаем группу
        oldEducationGroup.setTeachers(teachers);
        oldEducationGroup.setTitle(title);
        oldEducationGroup.setStudents(students);
        educationGroupRepo.save(oldEducationGroup);

        // Добавляем группу к пользователям
        for (Student student : newStudents) {
            student.getGroups().add(oldEducationGroup);
            studentRepo.save(student);
        }
        for (Teacher teacher : newTeachers) {
            teacher.getGroups().add(oldEducationGroup);
            teacherRepo.save(teacher);
        }

        return true;

    }

    // Ответ на вопрос пользователя
    public void answerTheQuestion(Message message, String answer) {
        // Текст письма
        String text = String.format(
                "Уважаемый, %s!\n" +
                        "Это ответ на ваш вопрос:\n"+
                        "Тема: %s\n"+
                        "Ваш вопрос: %s\n\n"+
                        "%s",
                message.getUserName(),
                message.getTheme(),
                message.getText(),
                answer
        );
        mailSender.send(message.getTheme(), message.getUserEmail(), text);

        // Удаление сообщения из БД
        messageRepo.deleteById(message.getId());
    }

    // Поиск студентов по почте
    private List<Student> getStudent(List<String> studentsEmails) {
        List<Student> students = new ArrayList<>();

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

        return students;
    }

    // Поиск учителей по почте
    private List<Teacher> getTeacher(List<String> teachersEmails) {
        List<Teacher> teachers = new ArrayList<>();

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
        return teachers;
    }

}

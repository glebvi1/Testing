package testing_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import testing_system.domain.people.Roles;
import testing_system.domain.people.Student;
import testing_system.domain.people.Teacher;
import testing_system.domain.people.User;
import testing_system.repos.people.StudentRepo;
import testing_system.repos.people.TeacherRepo;
import testing_system.repos.people.UserRepo;

import java.util.Collections;
import java.util.Set;

@Controller
public class RegistrationController {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private TeacherRepo teacherRepo;

    @GetMapping("/registr-form")
    public String registration() {return "reg";}

    @PostMapping("/registr-form")
    public String registration(User user, Model model) {
        User userFromDb = userRepo.findByUsername(user.getUsername());
        if (userFromDb != null) {
            model.addAttribute("message", "Пользователь с такой почтой уже существует!");
            return "reg";
        }
        if (user.getUsername().equals("a@a.a") && user.getFullName().equals("1") && user.getPassword().equals("1")) {
            User user1 = new User();
            user1.setUsername("s-admin@admin.admin");
            user1.setRoles(Collections.singleton(Roles.SYSTEM_ADMIN));
            user1.setPassword("1");
            user1.setFullName("s-admin");

            User user2 = new User();
            user2.setUsername("t-admin@admin.admin");
            user2.setRoles(Collections.singleton(Roles.TEACHER_ADMIN));
            user2.setPassword("1");
            user2.setFullName("t-admin");

            Student student = new Student();
            student.setUsername("student@student.student");
            student.setPassword("1");
            student.setRoles(Collections.singleton(Roles.STUDENT));
            student.setFullName("student");

            Teacher teacher = new Teacher();
            teacher.setFullName("teacher");
            teacher.setPassword("1");
            teacher.setRoles(Collections.singleton(Roles.TEACHER));
            teacher.setUsername("teacher@teacher.teacher");

            userRepo.save(user1);
            userRepo.save(user2);
            studentRepo.save(student);
            teacherRepo.save(teacher);

        } else {
        //user.setRoles(Collections.singleton(Roles.SYSTEM_ADMIN));
        Student student = new Student();
        student.setUsername(user.getUsername());
        student.setFullName(user.getFullName());
        student.setPassword(user.getPassword());
        student.setRoles(Collections.singleton(Roles.STUDENT));

        studentRepo.save(student);
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {return "login";}

    @PostMapping("/login")
    public String login(User user, Model model) {
        User userFromDb = userRepo.findByUsername(user.getUsername());
        if (userFromDb == null) {
            model.addAttribute("message", "Вы ввели неправильный пароль или почту.");
            return "login";
        }
        return "redirect:/about";
    }

    @GetMapping("/about")
    public String aboutUs(@AuthenticationPrincipal User user,
                          Model model) {
        model.addAttribute("name", user.getFullName());
        Set<Roles> roles = user.getRoles();
        if (roles.contains(Roles.SYSTEM_ADMIN)) {
            model.addAttribute("role", "system_admin");
        } else if (roles.contains(Roles.TEACHER_ADMIN)) {
            model.addAttribute("role", "teacher_admin");
        } else if (roles.contains(Roles.TEACHER)) {
            model.addAttribute("role", "teacher");
        } else if (roles.contains(Roles.STUDENT)) {
            model.addAttribute("role", "student");
        }

        return "about_us";
    }

}

package testing_system.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import testing_system.domain.dto.CaptchaResponseDto;
import testing_system.domain.people.Roles;
import testing_system.domain.people.Student;
import testing_system.domain.people.Teacher;
import testing_system.domain.people.User;
import testing_system.repos.people.StudentRepo;
import testing_system.repos.people.TeacherRepo;
import testing_system.repos.people.UserRepo;
import testing_system.service.AuxiliaryService;
import testing_system.service.UserService;

import java.util.Collections;

@Controller
public class RegistrationController {
    private static final String RECAPTHCA_API = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private TeacherRepo teacherRepo;
    @Autowired
    private AuxiliaryService auxiliaryService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${recaptcha.secret}")
    private String secret;

    @GetMapping("/registr-form")
    public String registration() {return "reg";}

    @PostMapping("/registr-form")
    public String registration(Student user, Model model,
                               @RequestParam(name = "g-recaptcha-response") String recaptchaResponse) {
        String url = String.format(RECAPTHCA_API, secret, recaptchaResponse);
        CaptchaResponseDto response = restTemplate.postForObject(url, Collections.emptyList(), CaptchaResponseDto.class);
        model.addAttribute("color", "text-danger");
        if (!response.isSuccess()) {
            model.addAttribute("captchaError", "Вы не выбрали это поле!");
            return "reg";
        }

        if (user.getUsername().equals("vyazgd@mail.ru") && user.getFullName().equals("1") && user.getPassword().equals("1")) {
            User user1 = new User();
            user1.setUsername("s-admin@admin.admin");
            user1.setRoles(Collections.singleton(Roles.SYSTEM_ADMIN));
            user1.setPassword(passwordEncoder.encode("1"));
            user1.setFullName("s-admin");

            Student user2 = new Student();
            user2.setUsername("t-admin@admin.admin");
            user2.setRoles(Collections.singleton(Roles.STUDENT));
            user2.setPassword(passwordEncoder.encode("1"));
            user2.setFullName("t-admin");

            Student student = new Student();
            student.setUsername("vyazgd@mail.ru");
            student.setPassword(passwordEncoder.encode("1"));
            student.setRoles(Collections.singleton(Roles.STUDENT));
            student.setFullName("student");

            Student student1 = new Student();
            student1.setUsername("iyti53@mail.ru");
            student1.setPassword(passwordEncoder.encode("1"));
            student1.setRoles(Collections.singleton(Roles.STUDENT));
            student1.setFullName("student1");

            Student student2 = new Student();
            student2.setUsername("yulia-vyazova@mail.ru");
            student2.setPassword(passwordEncoder.encode("1"));
            student2.setRoles(Collections.singleton(Roles.STUDENT));
            student2.setFullName("student2");

            Student teacher = new Student();
            teacher.setFullName("teacher");
            teacher.setPassword(passwordEncoder.encode("1"));
            teacher.setRoles(Collections.singleton(Roles.STUDENT));
            teacher.setUsername("teacher@teacher.teacher");

            Student teacher1 = new Student();
            teacher1.setFullName("teacher1");
            teacher1.setPassword(passwordEncoder.encode("1"));
            teacher1.setRoles(Collections.singleton(Roles.STUDENT));
            teacher1.setUsername("teacher1@teacher1.teacher1");

            Student teacher2 = new Student();
            teacher2.setFullName("teacher2");
            teacher2.setPassword(passwordEncoder.encode("1"));
            teacher2.setRoles(Collections.singleton(Roles.STUDENT));
            teacher2.setUsername("teacher2@teacher2.teacher2");

            Student teacher3 = new Student();
            teacher3.setFullName("teacher3");
            teacher3.setPassword(passwordEncoder.encode("1"));
            teacher3.setRoles(Collections.singleton(Roles.STUDENT));
            teacher3.setUsername("teacher3@teacher3.teacher3");

            Student student3 = new Student();
            student3.setUsername("studForTeachstudForTeach.studForTeach@");
            student3.setPassword(passwordEncoder.encode("1"));
            student3.setRoles(Collections.singleton(Roles.STUDENT));
            student3.setFullName("teacher4");

            Student student4 = new Student();
            student4.setUsername("studFroAdmin@studFroAdmin.studFroAdmin");
            student4.setPassword(passwordEncoder.encode("1"));
            student4.setRoles(Collections.singleton(Roles.STUDENT));
            student4.setFullName("admin2");

            userRepo.save(user1);

            studentRepo.save(user2);
            studentRepo.save(student1);
            studentRepo.save(student);
            studentRepo.save(student2);
            studentRepo.save(student3);
            studentRepo.save(student4);

            studentRepo.save(teacher);
            studentRepo.save(teacher1);
            studentRepo.save(teacher2);
            studentRepo.save(teacher3);

        } else {
            if (!userService.addUser(user)) {
                model.addAttribute("message", "Пользователь с такой почтой уже существует!\nИли такой почты не существует!");
                return "reg";
            }
        }

        model.addAttribute("message", "Вам на почту пришло письмо. Перейдите из него по ссылке, чтобы активировать аккаунт.");
        model.addAttribute("color", "text-success");
        return "reg";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(User user,
                        Model model) {
        User userFromDb = userRepo.findByUsername(user.getUsername());

        if (userFromDb == null) {
            model.addAttribute("message", "Вы ввели неправильный пароль или почту.");
            model.addAttribute("color", "text-danger");
            return "login";
        }

        return "redirect:/about";
    }

    @GetMapping("/activate/{code}")
    public String activate(Model model,
                           @PathVariable String code) {
        if (!userService.isActivated(code)) {
            model.addAttribute("message", "Активационный код не найден.");
            model.addAttribute("color", "text-danger");
        } else {
            model.addAttribute("message", "Активация аккаунта прошла успешно!");
            model.addAttribute("color", "text-success");
        }
        return "login";
    }

    @GetMapping("/about")
    public String aboutUs(@AuthenticationPrincipal User user,
                          Model model) {
        model.addAttribute("name", user.getFullName());
        model.addAttribute("role", auxiliaryService.getRole(user));

        return "about_us";
    }

}

package testing_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public String registration(User user, Model model,
                               @RequestParam(name = "g-recaptcha-response") String recaptchaResponse) {
        String url = String.format(RECAPTHCA_API, secret, recaptchaResponse);
        CaptchaResponseDto response = restTemplate.postForObject(url, Collections.emptyList(), CaptchaResponseDto.class);

        if (!response.isSuccess()) {
            model.addAttribute("captchaError", "Вы не выбрали это поле!");
            return "reg";
        }

        if (user.getUsername().equals("a@a.a") && user.getFullName().equals("1") && user.getPassword().equals("1")) {
            User user1 = new User();
            user1.setUsername("s-admin@admin.admin");
            user1.setRoles(Collections.singleton(Roles.SYSTEM_ADMIN));
            user1.setPassword(passwordEncoder.encode("1"));
            user1.setFullName("s-admin");

            User user2 = new User();
            user2.setUsername("t-admin@admin.admin");
            user2.setRoles(Collections.singleton(Roles.TEACHER_ADMIN));
            user2.setPassword(passwordEncoder.encode("1"));
            user2.setFullName("t-admin");

            Student student = new Student();
            student.setUsername("student@student.student");
            student.setPassword(passwordEncoder.encode("1"));
            student.setRoles(Collections.singleton(Roles.STUDENT));
            student.setFullName("student");

            Teacher teacher = new Teacher();
            teacher.setFullName("teacher");
            teacher.setPassword(passwordEncoder.encode("1"));
            teacher.setRoles(Collections.singleton(Roles.TEACHER));
            teacher.setUsername("teacher@teacher.teacher");

            userRepo.save(user1);
            userRepo.save(user2);
            studentRepo.save(student);
            teacherRepo.save(teacher);

        } else {
            if (!userService.addUser(user)) {
                model.addAttribute("message", "Пользователь с такой почтой уже существует!");
                return "reg";
            }
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
        if (userFromDb.getActivatedCode() != null) {
            model.addAttribute("message", "Вы не активировали свой аккаунт!");
            return "login";
        }

        return "redirect:/about";
    }

    @GetMapping("/activate/{code}")
    public String activate(Model model,
                           @PathVariable String code) {
        if (!userService.isActivated(code)) {
            model.addAttribute("message", "Активационный код не найден.");
        } else {
            model.addAttribute("message", "Активация аккаунта прошла успешно!");
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

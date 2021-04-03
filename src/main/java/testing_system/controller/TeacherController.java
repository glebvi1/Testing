package testing_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import testing_system.domain.group.EducationGroup;
import testing_system.domain.group.Module;
import testing_system.domain.people.Student;
import testing_system.domain.people.Teacher;
import testing_system.domain.people.Users;
import testing_system.domain.people.Roles;
import testing_system.domain.test.Question;
import testing_system.domain.test.Test;
import testing_system.repos.people.StudentRepo;
import testing_system.repos.people.TeacherRepo;
import testing_system.repos.people.UserRepo;
import testing_system.service.AuxiliaryService;
import testing_system.service.StudentService;
import testing_system.service.TeacherService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/educated")
@PreAuthorize("hasAnyAuthority('TEACHER', 'STUDENT', 'TEACHER_ADMIN')")
public class TeacherController {
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AuxiliaryService auxiliaryService;
    @Autowired
    private TeacherRepo teacherRepo;
    @Autowired
    private StudentService studentService;
    @Autowired
    private StudentRepo studentRepo;

    private int sectionCount = 5;

    // Группы, в которых пользователь является учителем
    @GetMapping
    @PreAuthorize("hasAnyAuthority('TEACHER', 'TEACHER_ADMIN')")
    public String myGroups(Model model,
                           @AuthenticationPrincipal Users user) {
        boolean isAdmin = user.getRoles().contains(Roles.TEACHER_ADMIN);
        boolean isTeacher = user.getRoles().contains(Roles.TEACHER);

        if (isAdmin && !isTeacher) {
            model.addAttribute("role", "teacher_admin");
        } else if (isAdmin && isTeacher) {
            model.addAttribute("role", "teacher_admin");
            model.addAttribute("allGroups", teacherRepo.findByUsername(user.getUsername()).getGroups());
        } else {
            model.addAttribute("role", "teacher");
            model.addAttribute("allGroups", teacherRepo.findByUsername(user.getUsername()).getGroups());
        }

        return "all_groups";
    }

    // Все участники группы
    @GetMapping("/course/{id}/all-participants")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'TEACHER_ADMIN')")
    public String allParticipants(Model model,
                                  @PathVariable(name = "id") EducationGroup educationGroup,
                                  @AuthenticationPrincipal Users user) {
        if (!auxiliaryService.security(user, educationGroup)) {
            return "error";
        }

        model.addAttribute("group", educationGroup);
        List<Users> list = new ArrayList<>();
        List<Float> stat = new ArrayList<>();
        list.addAll(educationGroup.getStudents());

        for (Student student : educationGroup.getStudents()) {
            stat.add(
                    Float.parseFloat(teacherService.statistics(
                    new ArrayList<>(student.getAllMarks().values()))[2]));
        }

        list.addAll(educationGroup.getTeachers());

        model.addAttribute("stat", stat);
        model.addAttribute("users", list);
        model.addAttribute("role", auxiliaryService.getRole(user));
        return "all_participants";
    }

    // Конкретная группа обучения
    @GetMapping("/course/{id}")
    public String course(@PathVariable(name = "id") EducationGroup educationGroup,
                         Model model,
                         @AuthenticationPrincipal Users user) {
        if (!auxiliaryService.security(user, educationGroup)) {
            return "error";
        }
        String role = auxiliaryService.getRole(user);


        if (role.equals("teacher_admin")) {
            model.addAttribute("role", role);
        } else if (role.equals("teacher")) {
            model.addAttribute("role", role);
        } else {
            model.addAttribute("role", "student");
        }
        model.addAttribute("group", educationGroup);
        model.addAttribute("groupId", educationGroup.getId());
        model.addAttribute("allModule", educationGroup.getModules());
        model.addAttribute("groupName", educationGroup.getTitle());

        return "course";
    }

    // Добавление модуля в группу обучения
    @PostMapping("/course/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public String addModule(@PathVariable(name = "id") EducationGroup educationGroup,
                            @RequestParam String title,
                            Model model,
                            @AuthenticationPrincipal Users user) {
        boolean isGoodTeacher = !educationGroup.getTeachers().contains(teacherRepo.findById(user.getId()).get());
        if (!auxiliaryService.security(user, educationGroup) ||
                isGoodTeacher) {
            return "error";
        }

        String role = auxiliaryService.getRole(user);

        if (role.equals("teacher_admin")) {
            model.addAttribute("role", role);
        } else {
            model.addAttribute("role", "teacher");
        }
        model.addAttribute("groupId", educationGroup.getId());

        if (!teacherService.addModule(educationGroup, title)) {
            model.addAttribute("allModule", educationGroup.getModules());
            model.addAttribute("message", "Модуль с таким названием уже существует.");
            return "course";
        }

        model.addAttribute("allModule", educationGroup.getModules());
        model.addAttribute("groupName", educationGroup.getTitle());

        return "course";
    }

    // Конкретный модуль в группе
    @GetMapping("/module/{id}")
    public String module(@PathVariable(name = "id") Module module,
                         Model model,
                         @AuthenticationPrincipal Users user) {
        if (!auxiliaryService.security(user, module.getEducationGroup())) {
            return "error";
        }

        if (user.getRoles().contains(Roles.STUDENT)) {
            model.addAttribute("role", "student");
        } else {
            model.addAttribute("role", teacherService.teacherOrAdmin(user, module));
        }

        model.addAttribute("module", module);
        model.addAttribute("tests", module.getTests());

        model.addAttribute("studentId", user.getId());

        return "module";
    }

    // Добавления теста (get)
    @GetMapping("/module/{id}/add-test")
    @PreAuthorize("hasAuthority('TEACHER')")
    public String addTest(@PathVariable(name = "id") Module module,
                          @RequestParam(name = "count", required = false) Integer count,
                          Model model,
                          @AuthenticationPrincipal Users user) {

        if (!auxiliaryService.security(user, module.getEducationGroup())) {
            return "error";
        }

        String role = auxiliaryService.getRole(user);
        if (role.equals("teacher_admin") && user.getRoles().contains(Roles.TEACHER)) {
            if (!teacherRepo.findById(user.getId()).get().getGroups().contains(module.getEducationGroup())) {
                return "redirect:/educated/module/" + module.getId();
            }
        }

        model.addAttribute("module", module);

        if (count == null || count <= 0 || count > 20) {
            model.addAttribute("message", "Кол-во вопросов не может быть больше 20.");
            count = 5;
        }

        model.addAttribute("count", fillArray(count));

        return "create_test";
    }

    // Добавления теста (post)
    @PostMapping("/module/{id}/add-test")
    @PreAuthorize("hasAuthority('TEACHER')")
    public String addTest(@RequestParam(name = "title") String title,
                          @RequestParam(name = "questions") List<String> htmlQuestions,
                          @RequestParam(name = "answers") List<String> htmlAnswersOptions,
                          @RequestParam(name = "isCorrect") List<String> htmlCorrectAnswers,
                          @PathVariable(name = "id") Module module,
                          @RequestParam(name = "marks") List<Integer> marks,
                          Model model,
                          @AuthenticationPrincipal Users user) {

        if (!auxiliaryService.security(user, module.getEducationGroup())) {
            return "error";
        }
        String role = auxiliaryService.getRole(user);
        if (role.equals("teacher_admin") && user.getRoles().contains(Roles.TEACHER)) {
            if (!teacherRepo.findById(user.getId()).get().getGroups().contains(module.getEducationGroup())) {
                return "redirect:/educated/module/" + module.getId();
            }
        }

        model.addAttribute("module", module);
        model.addAttribute("one", true);

        if (contains(module, title)) {
            model.addAttribute("message", "Тест с таким названием уже существует.");
            model.addAttribute("questions", htmlQuestions);
            model.addAttribute("answers", htmlAnswersOptions);
            model.addAttribute("isCorrect", htmlCorrectAnswers);
            model.addAttribute("marks", marks);
            return "create_test";
        }

        teacherService.addTest(title, htmlQuestions, htmlAnswersOptions, htmlCorrectAnswers,
                module, marks, 0);

        return "redirect:/educated/module/" + module.getId();
    }

    // Добавления билета (get)
    @GetMapping("/module/{id}/add-ticket")
    @PreAuthorize("hasAuthority('TEACHER')")
    public String addTicket(@PathVariable(name = "id") Module module,
                          @RequestParam(name = "countQuestions", required = false) Integer countQ,
                          @RequestParam(name = "countSections", required = false) Integer countS,
                          Model model,
                          @AuthenticationPrincipal Users user) {

        if (!auxiliaryService.security(user, module.getEducationGroup())) {
            return "error";
        }
        String role = auxiliaryService.getRole(user);
        if (role.equals("teacher_admin") && user.getRoles().contains(Roles.TEACHER)) {
            if (!teacherRepo.findById(user.getId()).get().getGroups().contains(module.getEducationGroup())) {
                return "redirect:/educated/module/" + module.getId();
            }
        }

        model.addAttribute("module", module);

        if (countQ == null || countQ <= 1 || countQ > 20) {
            countQ = 5;
            model.addAttribute("message", "Кол-во вопросов не может быть больше 20.");
        }
        if (countS == null || countS <= 0 || countS > 15) {
            model.addAttribute("message", "Кол-во разделов не может быть больше 15.");
            countS = 5;
        }
        sectionCount = countS;
        model.addAttribute("sectionsArr", fillArray(countS));
        model.addAttribute("questionsArr", fillArray(countQ));

        return "create_ticket";
    }

    // Добавления билета (post)
    @PostMapping("/module/{id}/add-ticket")
    @PreAuthorize("hasAuthority('TEACHER')")
    public String addTicket(@RequestParam(name = "title") String title,
                            @RequestParam(name = "questions") List<String> htmlQuestions,
                            @RequestParam(name = "answers") List<String> htmlAnswersOptions,
                            @RequestParam(name = "isCorrect") List<String> htmlCorrectAnswers,
                            @PathVariable(name = "id") Module module,
                            @RequestParam(name = "marks") List<Integer> marks,
                            Model model,
                            @AuthenticationPrincipal Users user) {

        if (!auxiliaryService.security(user, module.getEducationGroup())) {
            return "error";
        }
        String role = auxiliaryService.getRole(user);
        if (role.equals("teacher_admin") && user.getRoles().contains(Roles.TEACHER)) {
            if (!teacherRepo.findById(user.getId()).get().getGroups().contains(module.getEducationGroup())) {
                return "redirect:/educated/module/" + module.getId();
            }
        }

        model.addAttribute("module", module);

        if (contains(module, title)) {
            model.addAttribute("message", "Тест с таким названием уже существует.");
            model.addAttribute("questions", htmlQuestions);
            model.addAttribute("answers", htmlAnswersOptions);
            model.addAttribute("isCorrect", htmlCorrectAnswers);
            model.addAttribute("marks", marks);
            return "create_ticket";
        }

        teacherService.addTest(title, htmlQuestions, htmlAnswersOptions, htmlCorrectAnswers,
                module, marks, sectionCount);

        return "redirect:/educated/module/" + module.getId();
    }

    // Добавления теста с файлом (get)
    @GetMapping("/module/{id}/add-files")
    @PreAuthorize("hasAuthority('TEACHER')")
    public String addTestWithFile(@PathVariable(name = "id") Module module,
                                  Model model,
                                  @AuthenticationPrincipal Users user) {

        if (!auxiliaryService.security(user, module.getEducationGroup())) {
            return "error";
        }

        String role = auxiliaryService.getRole(user);
        if (role.equals("teacher_admin") && user.getRoles().contains(Roles.TEACHER)) {
            if (!teacherRepo.findById(user.getId()).get().getGroups().contains(module.getEducationGroup())) {
                return "redirect:/educated/module/" + module.getId();
            }
        }

        model.addAttribute("module", module);

        return "create_test_with_file";
    }

    // Добавления теста с файлом (post)
    @PostMapping("/module/{id}/add-files")
    @PreAuthorize("hasAuthority('TEACHER')")
    public String addTestWithFile(@PathVariable(name = "id") Module module,
                                  Model model,
                                  @AuthenticationPrincipal Users user,
                                  @RequestParam(name = "file") MultipartFile file,
                                  @RequestParam(name = "title") String title,
                                  @RequestParam String question) throws IOException {

        if (!auxiliaryService.security(user, module.getEducationGroup())) {
            return "error";
        }

        String role = auxiliaryService.getRole(user);
        if (role.equals("teacher_admin") && user.getRoles().contains(Roles.TEACHER)) {
            if (!teacherRepo.findById(user.getId()).get().getGroups().contains(module.getEducationGroup())) {
                return "redirect:/educated/module/" + module.getId();
            }
        }

        model.addAttribute("module", module);

        if (contains(module, title)) {
            model.addAttribute("message", "Тест с таким названием уже существует.");
            return "create_ticket";
        }

        teacherService.addTestWithFile(file, title, question, module);

        return "redirect:/educated/module/" + module.getId();
    }

    // Статистика выполнения теста
    @GetMapping("/test/{id}/statistics")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'TEACHER_ADMIN')")
    public String statistics(Model model,
                             @PathVariable(name = "id") Test test,
                             @AuthenticationPrincipal Users user) {

        if (!auxiliaryService.security(user, test.getModule().getEducationGroup())) {
            return "error";
        }
        studentService.initTest(test);
        List<Integer> allMarks = new ArrayList<>(test.getStudentsMarks().values());

        if (allMarks.size() != 0) {
            String[] strings = teacherService.statistics(allMarks);
            model.addAttribute("max", strings[0]);
            model.addAttribute("min", strings[1]);
            model.addAttribute("mean", strings[2]);
        }

        List<Users> allUsers = new ArrayList<>(30);

        for (long key : test.getStudentsMarks().keySet()) {
            allUsers.add(userRepo.findById(key).get());
        }

        model.addAttribute("allMarks", allMarks);
        model.addAttribute("users", allUsers);
        List<Question> questions = test.getQuestions();

        model.addAttribute("questions", questions);
        model.addAttribute("correct", teacherService.getCorrectAnswers(questions));
        model.addAttribute("test", test);
        if (test.getSections() != 0) {
            model.addAttribute("div1", test.getQuestions().size() / test.getSections());
        }
        model.addAttribute("module", test.getModule());

        model.addAttribute("role", teacherService.teacherOrAdmin(user, test.getModule()));

        return "statistics";
    }

    // Статистика выполнения теста с файлом
    @GetMapping("/test/{id}/statistics-with-files")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'TEACHER_ADMIN')")
    public String statisticsWithFiles(Model model,
                                      @PathVariable(name = "id") Test test,
                                      @AuthenticationPrincipal Users user) {

        if (!auxiliaryService.security(user, test.getModule().getEducationGroup())) {
            return "error";
        }

        List<Integer> allMarks = new ArrayList<>(test.getStudentsMarks().values());

        if (allMarks.size() != 0) {
            String[] strings = teacherService.statistics(allMarks);
            model.addAttribute("max", strings[0]);
            model.addAttribute("min", strings[1]);
            model.addAttribute("mean", strings[2]);
        }

        List<Student> allUsers = new ArrayList<>(30);

        for (long key : test.getStudentsSolving().keySet()) {
            allUsers.add(studentRepo.findById(key).get());
        }

        model.addAttribute("allMarks", allMarks);
        model.addAttribute("users", allUsers);
        model.addAttribute("questions", test.getQuestions());
        model.addAttribute("test", test);
        model.addAttribute("question", test.getQuestions().get(0).getQuestion());

        if (test.getSections() != 0) {
            model.addAttribute("div1", test.getQuestions().size() / test.getSections());
        }
        model.addAttribute("module", test.getModule());

        model.addAttribute("role", teacherService.teacherOrAdmin(user, test.getModule()));

        return "statistics_with_files";
    }

    // Проверка решения
    @GetMapping("/test/{id}/statistics-with-files/{id1}")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'TEACHER_ADMIN')")
    public String checkSolving(Model model,
                               @PathVariable(name = "id") Test test,
                               @PathVariable(name = "id1") Student student,
                               @AuthenticationPrincipal Users user) {
        if (!auxiliaryService.security(user, test.getModule().getEducationGroup())) {
            return "error";
        }

        String solving = test.getStudentsSolving().get(student.getId());
        model.addAttribute("files", solving.split(" "));
        model.addAttribute("info", student.getFullName());
        model.addAttribute("id", test.getId());
        model.addAttribute("id1", student.getId());

        return "check";
    }

    // Выставление оценки
    @PostMapping("/test/{id}/statistics-with-files/{id1}")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'TEACHER_ADMIN')")
    public String checkSolving(Model model,
                               @PathVariable(name = "id") Test test,
                               @PathVariable(name = "id1") Student student,
                               @AuthenticationPrincipal Users user,
                               @RequestParam(name = "mark") Integer mark) {
        if (!auxiliaryService.security(user, test.getModule().getEducationGroup())) {
            return "error";
        }

        String solving = test.getStudentsSolving().get(student.getId());
        model.addAttribute("files", solving.split(" "));

        if (mark <= 1 || mark > 5) {
            model.addAttribute("info", student.getFullName());
            model.addAttribute("id", test.getId());
            model.addAttribute("id1", student.getId());
            model.addAttribute("message", "Оценка должна находиться в интервале [2; 5]");
            return "check";
        }

        teacherService.putMark(student, test, mark);

        return "redirect:/educated/test/" + test.getId() + "/statistics-with-files";
    }

    @GetMapping("/test/{id}/comments/{id1}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public String takeComments(Model model,
                               @PathVariable(name = "id") Test test,
                               @PathVariable(name = "id1") Student student) {
        model.addAttribute("test", test);
        model.addAttribute("student", student);
        return "take_coment";
    }

    @PostMapping("/test/{id}/comments/{id1}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public String takeComments(@PathVariable(name = "id1") Student student,
                               @PathVariable(name = "id") Test test,
                               @RequestParam(name = "comm") String comments,
                               @RequestParam(name = "theme") String theme) {
        teacherService.takeComments(student, theme, comments);
        if (!test.isType()) {
            return "redirect:/educated/test/" + test.getId() + "/statistics";
        }
        return "redirect:/educated/test/" + test.getId() + "/statistics-with-files";
    }

    private int[] fillArray(int n) {
        int[] numbers = new int[n];
        for (int i = 0; i < n; i++) {
            numbers[i] = i + 1;
        }
        return numbers;
    }

    private boolean contains(Module module, String title) {
        for (Test tempTest : module.getTests()) {
            if (tempTest.getTitle().equals(title)) {
                return true;
            }
        }
        return false;
    }
}

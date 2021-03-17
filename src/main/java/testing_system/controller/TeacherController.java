package testing_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import testing_system.domain.group.EducationGroup;
import testing_system.domain.group.Module;
import testing_system.domain.people.Users;
import testing_system.domain.people.Roles;
import testing_system.domain.test.Question;
import testing_system.domain.test.Test;
import testing_system.repos.people.TeacherRepo;
import testing_system.repos.people.UserRepo;
import testing_system.service.AuxiliaryService;
import testing_system.service.TeacherService;

import java.util.ArrayList;
import java.util.List;

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
        list.addAll(educationGroup.getStudents());
        list.addAll(educationGroup.getTeachers());
        model.addAttribute("usersList", list);
        model.addAttribute("one", true);

        return "list_of_users";
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
        if (role.equals("teacher") || role.equals("teacher_admin")) {
            model.addAttribute("role", "teacher");
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
        if (!auxiliaryService.security(user, educationGroup)) {
            return "error";
        }
        model.addAttribute("role", "teacher");
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
        String role = auxiliaryService.getRole(user);
        if (role.equals("teacher") || role.equals("teacher_admin")) {
            model.addAttribute("role", "teacher");
        } else if (role.equals("student")){
            model.addAttribute("role", "student");
            model.addAttribute("studentId", user.getId());
        }
        model.addAttribute("module", module);
        model.addAttribute("tests", module.getTests());

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

        model.addAttribute("module", module);

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

    // Статистика выполнения теста
    @GetMapping("/test/{id}/statistics")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'TEACHER_ADMIN')")
    public String statistics(Model model,
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

        List<Users> allUsers = new ArrayList<>(30);

        for (long key : test.getStudentsMarks().keySet()) {
            allUsers.add(userRepo.findById(key).get());
        }

        model.addAttribute("allMarks", allMarks);
        model.addAttribute("users", allUsers);
        List<Question> questions = test.getQuestions();
        questions.removeIf(x -> contains2(questions, x.getId()));

        model.addAttribute("questions", questions);
        model.addAttribute("correct", getCorrectAnswers(questions));
        model.addAttribute("test", test);
        if (test.getSections() != 0) {
            model.addAttribute("div1", test.getQuestions().size() / test.getSections());
        }
        model.addAttribute("module", test.getModule());

        return "statistics";
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

    private List<String> getCorrectAnswers(List<Question> questions) {
        List<String> answers = new ArrayList<>();

        for (Question question : questions) {
            List<String> answersOptions = question.getAnswersOptions();
            if (answersOptions.size() == 1) {
                answers.add(answersOptions.get(0));
            } else {
                List<Boolean> correctAnswers = question.getCorrectAnswer();
                String cor = "";

                for (int i = 0; i < answersOptions.size(); i++) {
                    if (correctAnswers.get(i)) {
                        cor += answersOptions.get(i) + ", ";
                    }
                }
                answers.add(cor.substring(0, cor.length() - 2));
            }
        }

        return answers;
    }

    private boolean contains2(List<Question> questions, long id) {
        int count = 0;
        for (Question question : questions) {
            if (count >= 2) {
                return true;
            }
            if (question.getId() == id) {
                count++;
            }
        }
        return count >= 2;
    }

}

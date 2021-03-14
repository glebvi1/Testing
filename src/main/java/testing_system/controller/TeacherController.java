package testing_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import testing_system.domain.group.EducationGroup;
import testing_system.domain.group.Module;
import testing_system.domain.people.Roles;
import testing_system.domain.people.Teacher;
import testing_system.domain.people.User;
import testing_system.domain.test.Test;
import testing_system.repos.group.EducationGroupRepo;
import testing_system.repos.group.ModuleRepo;
import testing_system.repos.people.UserRepo;
import testing_system.repos.test.TestRepo;
import testing_system.service.AuxiliaryService;
import testing_system.service.TeacherService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/educated")
@PreAuthorize("hasAnyAuthority('TEACHER', 'STUDENT', 'TEACHER_ADMIN')")
public class TeacherController {
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private TestRepo testRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AuxiliaryService auxiliaryService;

    @GetMapping
    @PreAuthorize("hasAuthority('TEACHER')")
    public String myGroups(Model model,
                           @AuthenticationPrincipal Teacher teacher) {
        List<EducationGroup> groups;
        groups = teacher.getGroups();

        model.addAttribute("allGroups", groups);
        model.addAttribute("role", "teacher");

        return "all_groups";
    }

    @GetMapping("/course/{id}/all-participants")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'TEACHER_ADMIN')")
    public String allParticipants(Model model,
                                  @PathVariable(name = "id") EducationGroup educationGroup,
                                  @AuthenticationPrincipal User user) {
        if (!auxiliaryService.security(user, educationGroup)) {
            return "error";
        }

        model.addAttribute("group", educationGroup);
        List<User> list = new ArrayList<>();
        list.addAll(educationGroup.getStudents());
        list.addAll(educationGroup.getTeachers());
        model.addAttribute("usersList", list);

        return "list_of_users";
    }

    @GetMapping("/course/{id}")
    public String course(@PathVariable(name = "id") EducationGroup educationGroup,
                         Model model,
                         @AuthenticationPrincipal User user) {
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

    @PostMapping("/course/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public String addModule(@PathVariable(name = "id") EducationGroup educationGroup,
                            @RequestParam String title,
                            Model model,
                            @AuthenticationPrincipal User user) {
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

    @GetMapping("/module/{id}")
    public String module(@PathVariable(name = "id") Module module,
                         Model model,
                         @AuthenticationPrincipal User user) {
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

    @GetMapping("/module/{id}/add-test")
    @PreAuthorize("hasAuthority('TEACHER')")
    public String addTest(@PathVariable(name = "id") Module module,
                          @RequestParam(name = "count", required = false) Integer count,
                          Model model,
                          @AuthenticationPrincipal User user) {

        if (!auxiliaryService.security(user, module.getEducationGroup())) {
            return "error";
        }

        model.addAttribute("module", module);

        if (count == null || count <= 0) {
            count = 5;
        }
        int[] numbers = new int[count];

        for (int i = 0; i < count; i++) {
            numbers[i] = i + 1;
        }
        model.addAttribute("count", numbers);

        return "create_test";
    }

    @PostMapping("/module/{id}/add-test")
    @PreAuthorize("hasAuthority('TEACHER')")
    public String addTest(@RequestParam(name = "title") String title,
                          @RequestParam(name = "questions") List<String> htmlQuestions,
                          @RequestParam(name = "answers") List<String> htmlAnswersOptions,
                          @RequestParam(name = "isCorrect") List<String> htmlCorrectAnswers,
                          @PathVariable(name = "id") Module module,
                          @RequestParam(name = "marks") List<Integer> marks,
                          Model model,
                          User user) {

        if (!auxiliaryService.security(user, module.getEducationGroup())) {
            return "error";
        }

        model.addAttribute("module", module);

        Test testFromDb = testRepo.findTestByTitle(title);

        if (testFromDb != null) {
            model.addAttribute("message", "Тест с таким названием уже существует.");
            model.addAttribute("questions", htmlQuestions);
            model.addAttribute("answers", htmlAnswersOptions);
            model.addAttribute("isCorrect", htmlCorrectAnswers);
            model.addAttribute("marks", marks);
            return "create_test";
        }

        teacherService.addTest(title, htmlQuestions, htmlAnswersOptions, htmlCorrectAnswers,
                module, marks);

        return "redurect:/educated/module/" + module.getId();
    }

    @GetMapping("/test/{id}/statistics")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'TEACHER_ADMIN')")
    public String statistics(Model model,
                             @PathVariable(name = "id") Test test,
                             @AuthenticationPrincipal User user) {

        if (!auxiliaryService.security(user, test.getModule().getEducationGroup())) {
            return "error";
        }

        model.addAttribute("role", auxiliaryService.getRole(user));

        List<Integer> allMarks = new ArrayList<>(test.getStudentsMarks().values());

        if (allMarks.size() != 0) {
            String[] strings = teacherService.statistics(allMarks);

            model.addAttribute("max", strings[0]);
            model.addAttribute("min", strings[1]);
            model.addAttribute("mean", strings[2]);
        }

        List<User> users = new ArrayList<>(30);

        for (long key : test.getStudentsMarks().keySet()) {
            users.add(userRepo.findById(key).get());
        }

        model.addAttribute("allMarks", allMarks);
        model.addAttribute("users", users);

        return "statistics";
    }



}

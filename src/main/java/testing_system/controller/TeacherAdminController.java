package testing_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import testing_system.domain.group.EducationGroup;
import testing_system.domain.people.Roles;
import testing_system.domain.people.User;
import testing_system.repos.group.EducationGroupRepo;
import testing_system.repos.people.StudentRepo;
import testing_system.repos.people.TeacherRepo;
import testing_system.repos.people.UserRepo;
import testing_system.service.AuxiliaryService;
import testing_system.service.SystemAdminService;
import testing_system.service.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher-admin")
@PreAuthorize("hasAnyAuthority('TEACHER_ADMIN', 'SYSTEM_ADMIN')")
public class TeacherAdminController {
    @Autowired
    private EducationGroupRepo educationGroupRepo;
    @Autowired
    private AuxiliaryService auxiliaryService;
    @Autowired
    private SystemAdminService systemAdminService;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private TeacherRepo teacherRepo;
    @Autowired
    private UserRepo userRepo;

    @GetMapping("/all-users")
    public String listUsers(Model model,
                            @RequestParam(required = false) String username,
                            @RequestParam(required = false) String fullName,
                            @AuthenticationPrincipal User user) {
        model.addAttribute("admin", true);
        if (user.getRoles().contains(Roles.SYSTEM_ADMIN)) {
            model.addAttribute("sys_admin", true);
        }
        if (!StringUtils.isEmpty(username) || !StringUtils.isEmpty(fullName)) {
            model.addAttribute("usersList", systemAdminService.sort(username, fullName));
        } else {
            List<User> users = new ArrayList<User>();
            users.addAll(studentRepo.findAll());
            users.addAll(teacherRepo.findAll());
            users.addAll(userRepo.findAll().stream()
                .filter(tUser -> tUser.getRoles().contains(Roles.TEACHER_ADMIN) &&
                        !tUser.getRoles().contains(Roles.TEACHER))
                .collect(Collectors.toList()));
            model.addAttribute("usersList", users);
        }
        return "list_of_users";
    }

    @GetMapping("/all-groups")
    public String allGroups(Model model,
                            @AuthenticationPrincipal User user) {
        List<EducationGroup> educationGroups;
        educationGroups = educationGroupRepo.findAll();

        model.addAttribute("allGroups", educationGroups);
        model.addAttribute("role", auxiliaryService.getRole(user));

        return "all_groups";
    }

    @GetMapping("/all-groups/edit/{id}")
    public String editGroup(@PathVariable(name = "id") EducationGroup educationGroup,
                            Model model,
                            @AuthenticationPrincipal User user) {
        model.addAttribute("id", educationGroup.getId());
        model.addAttribute("role", auxiliaryService.getRole(user));

        int countS = educationGroup.getStudents().size();
        int[] studentsNumbers = new int[30 - countS];
        for (int i = 0; i < 30 - countS; i++) {
            studentsNumbers[i] = i + countS + 1;
        }

        countS = educationGroup.getTeachers().size();
        int[] teacherNumbers = new int[10 - countS];
        for (int i = 0; i < 10 - countS; i++) {
            teacherNumbers[i] = i + countS + 1;
        }

        model.addAttribute("teacherNumbers", teacherNumbers);
        model.addAttribute("studentsNumbers", studentsNumbers);

        return "edit_group";
    }

    @PostMapping("/all-groups/edit/{id}")
    public String editGroup(@RequestParam String title,
                            @RequestParam(name = "studentsEmails") List<String> studentsEmails,
                            @RequestParam(name = "teachersEmails") List<String> teachersEmails,
                            Model model,
                            @AuthenticationPrincipal User user,
                            @PathVariable(name = "id") EducationGroup educationGroup) {

        if (!systemAdminService.updateEducationGroup(studentsEmails, teachersEmails, title, educationGroup)) {
            model.addAttribute("message", "Проверьте поля ввода.");
            model.addAttribute("role", auxiliaryService.getRole(user));
            return "edit_group";
        }


        return "redirect:/teacher-admin/all-groups";
    }

}

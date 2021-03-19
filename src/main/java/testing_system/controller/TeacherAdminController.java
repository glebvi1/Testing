package testing_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import testing_system.domain.group.EducationGroup;
import testing_system.domain.people.Users;
import testing_system.domain.people.Roles;
import testing_system.repos.group.EducationGroupRepo;
import testing_system.repos.people.UserRepo;
import testing_system.service.AuxiliaryService;
import testing_system.service.SystemAdminService;

import java.util.ArrayList;
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
    private UserRepo userRepo;

    // Список всех пользователей
    @GetMapping("/all-users")
    public String listUsers(Model model,
                            @RequestParam(required = false) String username,
                            @RequestParam(required = false) String fullName,
                            @AuthenticationPrincipal Users user,
                            @PageableDefault(sort = { "id" }, direction = Sort.Direction.DESC) Pageable pageable) {

        if (user.getRoles().contains(Roles.SYSTEM_ADMIN)) {
            model.addAttribute("sys_admin", true);
            model.addAttribute("admin", false);
        } else {
            model.addAttribute("admin", true);
        }
        model.addAttribute("some_admin", true);
        if (!StringUtils.isEmpty(username) || !StringUtils.isEmpty(fullName)) {
            model.addAttribute("usersList", systemAdminService.sort(username, fullName));
            model.addAttribute("one", true);
        } else {
            List<Users> allUsers = userRepo.findAll(pageable)
                    .stream().filter(tUser -> !tUser.getRoles().contains(Roles.SYSTEM_ADMIN))
                    .collect(Collectors.toList());
            Page<Users> pages = new PageImpl<>(allUsers);
            model.addAttribute("one", false);
            if (allUsers.size() == 0) {
                model.addAttribute("usersList", new PageImpl<Users>(new ArrayList<>()));
            } else {
                model.addAttribute("usersList", pages);
            }
            int n = allUsers.size() / 10 + 2;
            int[] arr = new int[n];
            for (int i = 0; i < n; i++) {
                arr[i] = i + 1;
            }
            model.addAttribute("arr", arr);

        }
        return "list_of_users";
    }

    // Список всех групп
    @GetMapping("/all-groups")
    public String allGroups(Model model,
                            @AuthenticationPrincipal Users user,
                            @PageableDefault(sort = { "id" }, direction = Sort.Direction.DESC) Pageable pageable) {
        List<EducationGroup> educationGroups = educationGroupRepo.findAll();

        Page<EducationGroup> pageGroups = educationGroupRepo.findAll(pageable);

        model.addAttribute("one", false);
        if (educationGroups.size() == 0) {
            model.addAttribute("usersList", new PageImpl<Users>(new ArrayList<>()));
        } else {
            model.addAttribute("usersList", pageGroups);
        }
        int n = educationGroups.size() / 10 + 2;
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = i + 1;
        }
        model.addAttribute("arr", arr);
        model.addAttribute("allGroups", pageGroups);
        model.addAttribute("role", auxiliaryService.getRole(user));
        model.addAttribute("one", false);

        return "all_groups";
    }

    // Изменение группы
    @GetMapping("/all-groups/edit/{id}")
    public String editGroup(@PathVariable(name = "id") EducationGroup educationGroup,
                            Model model,
                            @AuthenticationPrincipal Users user) {
        model.addAttribute("id", educationGroup.getId());
        model.addAttribute("role", auxiliaryService.getRole(user));
        model.addAttribute("groupName", educationGroup.getTitle());

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
                            @AuthenticationPrincipal Users user,
                            @PathVariable(name = "id") EducationGroup educationGroup) {

        if (!systemAdminService.updateEducationGroup(studentsEmails, teachersEmails, title, educationGroup)) {
            model.addAttribute("message", "Проверьте поля ввода.");
            model.addAttribute("role", auxiliaryService.getRole(user));
            model.addAttribute("groupName", educationGroup.getTitle());
            return "edit_group";
        }


        return "redirect:/teacher-admin/all-groups";
    }

}

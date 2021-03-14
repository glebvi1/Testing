package testing_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import testing_system.domain.message.Message;
import testing_system.domain.people.Roles;
import testing_system.domain.people.User;
import testing_system.repos.message.MessageRepo;
import testing_system.repos.people.StudentRepo;
import testing_system.repos.people.TeacherRepo;
import testing_system.repos.people.UserRepo;
import testing_system.service.SystemAdminService;

import java.util.List;
import java.util.Map;

@Controller
@PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
@RequestMapping("/system-admin")
public class SystemAdminController {
    @Autowired
    private SystemAdminService systemAdminService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private MessageRepo messageRepo;

    @GetMapping("/edit/{id}")
    public String edit(Model model,
                       @PathVariable Long id) {
        model.addAttribute("user", userRepo.findById(id).get());
        model.addAttribute("roles", Roles.values());
        return "edit_role";
    }

    @PostMapping("/edit/{id}")
    public String edit(@RequestParam Map<String, String> htmlRoles,
                       @PathVariable(name = "id") User user,
                       Model model) {

        if (!systemAdminService.editUser(user, htmlRoles)) {
            model.addAttribute("user", user);
            model.addAttribute("roles", Roles.values());
            model.addAttribute("message", "Вы ввели не корректные данные.");
            return "edit_role";
        }

        return "redirect:/teacher-admin/all-users";
    }

    @GetMapping("/add-group")
    public String addGroup(Model model) {

        int[] studentsNumbers = new int[30];
        for (int i = 1; i <= 30; i++) {
            studentsNumbers[i - 1] = i;
        }
        int[] teacherNumbers = new int[10];
        for (int i = 1; i <= 10; i++) {
            teacherNumbers[i - 1] = i;
        }

        model.addAttribute("studentsNumbers", studentsNumbers);
        model.addAttribute("teacherNumbers", teacherNumbers);
        return "create_group";
    }

    @PostMapping("/add-group")
    public String addGroup(@RequestParam String title,
                           @RequestParam(name = "studentsEmails") List<String> studentsEmails,
                           @RequestParam(name = "teachersEmails") List<String> teachersEmails,
                           Model model) {
        if (!systemAdminService.createEducationGroup(studentsEmails, teachersEmails, title)) {
            model.addAttribute("message", "Проверьте поля ввода.");
            return "redirect:/create_group";
        }

        return "redirect:/teacher-admin/all-users";
    }

    @GetMapping("/questions")
    public String questions(Model model) {
        model.addAttribute("messages", messageRepo.findAll());
        return "list_of_message";
    }

    @GetMapping("/questions/{id}")
    public String getAnswer(@PathVariable(name = "id") Message message,
                            Model model) {
        model.addAttribute("message", message);
        return "interaction_admin";
    }

    @PostMapping("/questions/{id}")
    public String getAnswer(@PathVariable(name = "id") Message message,
                            Model model,
                            @RequestParam String answer) {
        model.addAttribute("message", message);
        systemAdminService.answerTheQuestion(message, answer);
        return "redirect:/system-admin/questions";
    }

}

package testing_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import testing_system.domain.group.EducationGroup;
import testing_system.repos.group.EducationGroupRepo;

import java.util.List;

@Controller
@RequestMapping("/teacher-admin")
@PreAuthorize("hasAuthority('TEACHER_ADMIN')")
public class TeacherAdminController {
    @Autowired
    private EducationGroupRepo educationGroupRepo;

    @GetMapping("/all-groups")
    public String allGroups(Model model) {
        List<EducationGroup> educationGroups;
        educationGroups = educationGroupRepo.findAll();

        model.addAttribute("allGroups", educationGroups);
        model.addAttribute("role", "admin");

        return "all_groups";
    }

}

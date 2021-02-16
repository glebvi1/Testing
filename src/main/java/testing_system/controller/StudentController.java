package testing_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import testing_system.domain.people.Student;
import testing_system.domain.test.Test;
import testing_system.service.StudentService;

import java.util.List;

@Controller
@RequestMapping("/student")
@PreAuthorize("hasAuthority('STUDENT')")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    public String allGroup(@AuthenticationPrincipal Student student,
                           Model model) {

        model.addAttribute("allGroups", student.getGroups());
        model.addAttribute("role", "student");

        return "all_groups";
    }

    @GetMapping("/test/{id}")
    public String doTest(@PathVariable(name = "id") Test test,
                         Model model,
                         @AuthenticationPrincipal Student student) {
        if (test.getStudentsMarks().containsKey(student.getId())) {
            return "redirect:/educated";
        }
        model.addAttribute("isDone", false);
        model.addAttribute("test", test);

        return "test";
    }

    @PostMapping("/test/{id}")
    public String doTest(@PathVariable(name = "id") Test test,
                         @RequestParam(name = "answers") List<String> htmlAnswers,
                         Model model,
                         @AuthenticationPrincipal Student student) {

        model.addAttribute("test", test);

        int mark = studentService.doTest(test, htmlAnswers, student);

        model.addAttribute("mark", mark);
        model.addAttribute("isDone", true);

        return "test";
    }

}

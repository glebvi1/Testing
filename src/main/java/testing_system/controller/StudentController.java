package testing_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import testing_system.domain.people.Student;
import testing_system.domain.test.Question;
import testing_system.domain.test.Test;
import testing_system.service.StudentService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/student")
@PreAuthorize("hasAuthority('STUDENT')")
public class StudentController {

    private Test generatedTestFromTicket = null;

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
            return "redirect:/educated/module/" + test.getModule().getId();
        }

        List<Question> questions = test.getQuestions();
        List<Question> deleted = new ArrayList<>();
        for (Question question : questions) {
            if (!deleted.contains(question)) {
                deleted.add(question);
            }
        }
        test.setQuestions(deleted);

        if (test.getSections() == 0) {
            model.addAttribute("questions", deleted);
            model.addAttribute("test", test);
        } else if (generatedTestFromTicket == null || generatedTestFromTicket.getId() != test.getId()){
            generatedTestFromTicket = studentService.generateTestFromTicket(test);
            model.addAttribute("test", generatedTestFromTicket);
            model.addAttribute("questions", generatedTestFromTicket.getQuestions());
        } else {
            model.addAttribute("test", generatedTestFromTicket);
            model.addAttribute("questions", generatedTestFromTicket.getQuestions());
        }

        model.addAttribute("isDone", false);

        return "test";
    }

    @PostMapping("/test/{id}")
    public String doTest(@PathVariable(name = "id") Test test,
                         @RequestParam(name = "answers") List<String> htmlAnswers,
                         Model model,
                         @AuthenticationPrincipal Student student) {

        model.addAttribute("test", test);

        int mark;
        if (generatedTestFromTicket == null) {
            mark = studentService.doTest(test, htmlAnswers, student);
        } else {
            mark = studentService.doTicket(generatedTestFromTicket, test, htmlAnswers, student);
            generatedTestFromTicket = null;
        }
        model.addAttribute("mark", mark);
        model.addAttribute("isDone", true);

        return "redirect:/educated/module/" + test.getModule().getId();
    }

}

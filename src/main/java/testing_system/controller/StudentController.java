package testing_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import testing_system.domain.people.Student;
import testing_system.domain.test.Question;
import testing_system.domain.test.Test;
import testing_system.repos.test.TestRepo;
import testing_system.service.StudentService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/student")
@PreAuthorize("hasAuthority('STUDENT')")
public class StudentController {

    private Test generatedTestFromTicket = null;

    @Autowired
    private StudentService studentService;
    @Autowired
    private TestRepo testRepo;

    // Все учебные группы студента
    @GetMapping
    public String allGroup(@AuthenticationPrincipal Student student,
                           Model model) {

        model.addAttribute("allGroups", student.getGroups());
        model.addAttribute("role", "student");
        model.addAttribute("one", true);

        return "all_groups";
    }

    // Прохождения теста
    @GetMapping("/test/{id}")
    public String doTest(@PathVariable(name = "id") long id,
                         Model model,
                         @AuthenticationPrincipal Student student) {
        Test test = testRepo.findById(id).get();
        if (test.getStudentsMarks().containsKey(student.getId())) {
            return "redirect:/educated/module/" + test.getModule().getId();
        }

        List<Question> questions = studentService.initTest(test);

        if (!StringUtils.isEmpty(test.getFilename())) {
            model.addAttribute("questions", questions.get(0).getQuestion());
            model.addAttribute("test", test);
            model.addAttribute("file", true);
            return "do_test_with_files";
        } else if (test.getSections() == 0) {
            model.addAttribute("questions", questions);
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

    // Отпрвка выбранных ответов, выставление оценки
    @PostMapping("/test/{id}")
    public String doTest(@PathVariable(name = "id") long id,
                         @RequestParam(name = "answers", required = false) List<String> htmlAnswers,
                         Model model,
                         @AuthenticationPrincipal Student student,
                         @RequestParam(name = "files", required = false) List<MultipartFile> files) throws IOException {
        Test test = testRepo.findById(id).get();

        model.addAttribute("test", test);
        int mark = 0;
        if (!StringUtils.isEmpty(test.getFilename())) {
            studentService.doTestWithFile(test, files, student);
        } else if (generatedTestFromTicket == null) {
            studentService.initTest(test);
            mark = studentService.doTest(test, htmlAnswers, student);
        } else {
            studentService.initTest(test);
            mark = studentService.doTicket(generatedTestFromTicket, test, htmlAnswers, student);
            generatedTestFromTicket = null;
        }
        model.addAttribute("mark", mark);
        model.addAttribute("isDone", true);

        return "redirect:/educated/module/" + test.getModule().getId();
    }

}

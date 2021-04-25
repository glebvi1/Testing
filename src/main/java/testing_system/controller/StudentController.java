package testing_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import testing_system.domain.people.Student;
import testing_system.domain.test.Question;
import testing_system.domain.test.Test;
import testing_system.repos.test.TestRepo;
import testing_system.service.StudentService;
import testing_system.service.TeacherService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/student")
@PreAuthorize("hasAuthority('STUDENT')")
public class StudentController {
    @Autowired
    private StudentService studentService;
    @Autowired
    private TestRepo testRepo;
    @Autowired
    private TeacherService teacherService;

    // Все учебные группы студента
    @GetMapping
    public String allGroup(@AuthenticationPrincipal Student student,
                           Model model) {

        model.addAttribute("allGroups", student.getGroups());
        model.addAttribute("role", "student");
        model.addAttribute("one", true);

        return "all_groups";
    }

    @GetMapping("/stat")
    public String studentStatistics(Model model,
                                    @AuthenticationPrincipal Student student) {
        String[] strings = teacherService.statistics(
                new ArrayList<>(student.getAllMarks().values()));

        model.addAttribute("max", strings[0]);
        model.addAttribute("min", strings[1]);
        model.addAttribute("mean", strings[2]);

        return "s_statistics";
    }

    // Прохождения теста
    @GetMapping("/test/{id}")
    public String doTest(@PathVariable(name = "id") long id,
                         Model model,
                         @AuthenticationPrincipal Student student) {
        Test test = testRepo.findById(id).get();
        if (test.getStudentsMarks().containsKey(student.getId())
                || (test.getModule().getControlWork().containsKey(student.getId()) && test.getSections() != 0)) {
            return "redirect:/educated/module/" + test.getModule().getId();
        }

        List<Question> questions = studentService.initTest(test);

        // Тест с прикрепленным файлом
        if (test.isIsFile()) {
            model.addAttribute("question", questions.get(0).getQuestion());
            model.addAttribute("test", test);
            model.addAttribute("file", true);
            return "do_test_with_files";
        // Обычный тест
        } else if (test.getSections() == 0) {
            model.addAttribute("questions", questions);
            model.addAttribute("test", test);
        // Контрольная работа, генерируется первый раз
        } else if (!test.getModule().getControlWork().containsKey(student.getId())) {
            Test generatedTest = studentService.generateTestFromTicket(test, student);
            model.addAttribute("test", generatedTest);
            model.addAttribute("questions", generatedTest.getQuestions());
        // Котрольная работа, уже была сгенерированна
        } else {
            Long testId = test.getModule().getControlWork().get(student.getId());
            Test generatedTest = testRepo.findById(testId).get();
            studentService.initTest(generatedTest);
            model.addAttribute("test", generatedTest);
            model.addAttribute("questions", generatedTest.getQuestions());
        }

        model.addAttribute("isDone", false);

        return "test";
    }

    // Отпрвка выбранных ответов, выставление оценки
    @PostMapping("/test/{id}")
    public String doTest(@PathVariable(name = "id") Test test,
                         @RequestParam(name = "answers", required = false) List<String> htmlAnswers,
                         Model model,
                         @AuthenticationPrincipal Student student,
                         @RequestParam(name = "files", required = false) List<MultipartFile> files) throws IOException {
        if (htmlAnswers == null && files == null) {
            return "redirect:/student/test/" + test.getId();
        }

        if (files != null) {
            int count = 0;
            for (MultipartFile f : files) {
                count += f.getSize();
            }
            if (count >= 10000000) {
                return "redirect:/student/test/" + test.getId();
            }
        }

        model.addAttribute("test", test);
        int mark = 0;
        if (test.isIsFile()) {
            studentService.doTestWithFile(test, files, student);
        } else if (test.isControl()) {
            studentService.initTest(test);
            mark = studentService.doTicket(test, htmlAnswers, student);
        } else {
            studentService.initTest(test);
            mark = studentService.doTest(test, htmlAnswers, student);
        }
        model.addAttribute("mark", mark);
        model.addAttribute("isDone", true);

        return "redirect:/educated/module/" + test.getModule().getId();
    }

}

package testing_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import testing_system.domain.people.Student;
import testing_system.domain.people.Users;
import testing_system.domain.test.Question;
import testing_system.domain.test.StudentsAnswers;
import testing_system.domain.test.Test;
import testing_system.repos.people.StudentRepo;
import testing_system.repos.people.UserRepo;
import testing_system.repos.test.TestRepo;
import testing_system.service.AuxiliaryService;
import testing_system.service.StudentService;
import testing_system.service.TeacherService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/educated")
@PreAuthorize("hasAnyAuthority('TEACHER', 'STUDENT', 'TEACHER_ADMIN')")
public class StatisticsController {
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AuxiliaryService auxiliaryService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private TestRepo testRepo;

    // Статистика выполнения теста
    @GetMapping("/test/{id}/statistics")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'TEACHER_ADMIN')")
    public String statistics(Model model,
                             @PathVariable(name = "id") Test test,
                             @AuthenticationPrincipal Users user) {

        if (!auxiliaryService.security(user, test.getModule().getEducationGroup())) {
            return "error";
        }
        studentService.initTest(test);
        List<Users> allUsers = new ArrayList<>(30);
        List<Integer> allMarks = new ArrayList<>();

        if (test.getSections() == 0) {
            allMarks = new ArrayList<>(test.getStudentsMarks().values());
            for (long key : test.getStudentsMarks().keySet()) {
                allUsers.add(userRepo.findById(key).get());
            }
        } else {
            for (long key : test.getModule().getControlWork().keySet()) {
                allUsers.add(userRepo.findById(key).get());
            }
            for (long id : test.getModule().getControlWork().values()) {
                int mark = new ArrayList<>(testRepo.findById(id).get().getStudentsMarks().values()).get(0);
                allMarks.add(mark);
            }
        }

        model.addAttribute("allMarks", allMarks);
        if (allMarks.size() != 0) {
            String[] strings = teacherService.statistics(allMarks);
            model.addAttribute("max", strings[0]);
            model.addAttribute("min", strings[1]);
            model.addAttribute("mean", strings[2]);
        }

        model.addAttribute("users", allUsers);
        List<Question> questions = test.getQuestions();

        model.addAttribute("questions", questions);
        model.addAttribute("correct", teacherService.getCorrectAnswers(questions));
        model.addAttribute("test", test);
        if (test.getSections() != 0) {
            model.addAttribute("div1", test.getQuestions().size() / test.getSections());
        }
        model.addAttribute("module", test.getModule());

        model.addAttribute("role", teacherService.teacherOrAdmin(user, test.getModule()));
        model.addAttribute("titles", test.getSectionTitle());

        return "statistics";
    }

    // Статистика выполнения теста с файлом
    @GetMapping("/test/{id}/statistics-with-files")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'TEACHER_ADMIN')")
    public String statisticsWithFiles(Model model,
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

        List<Student> allUsers = new ArrayList<>(30);

        for (long key : test.getStudentsSolving().keySet()) {
            allUsers.add(studentRepo.findById(key).get());
        }

        model.addAttribute("allMarks", allMarks);
        model.addAttribute("users", allUsers);
        model.addAttribute("questions", test.getQuestions());
        model.addAttribute("test", test);
        model.addAttribute("question", test.getQuestions().get(0).getQuestion());

        if (test.getSections() != 0) {
            model.addAttribute("div1", test.getQuestions().size() / test.getSections());
        }
        model.addAttribute("module", test.getModule());

        model.addAttribute("role", teacherService.teacherOrAdmin(user, test.getModule()));

        return "statistics_with_files";
    }

    // Проверка решения
    @GetMapping("/test/{id}/statistics-with-files/{id1}")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'TEACHER_ADMIN')")
    public String checkSolving(Model model,
                               @PathVariable(name = "id") Test test,
                               @PathVariable(name = "id1") Student student,
                               @AuthenticationPrincipal Users user) {
        if (!auxiliaryService.security(user, test.getModule().getEducationGroup())) {
            return "error";
        }

        String solving = test.getStudentsSolving().get(student.getId());
        model.addAttribute("files", solving.split(" "));
        model.addAttribute("info", student.getFullName());
        model.addAttribute("id", test.getId());
        model.addAttribute("id1", student.getId());

        return "check";
    }

    // Выставление оценки
    @PostMapping("/test/{id}/statistics-with-files/{id1}")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'TEACHER_ADMIN')")
    public String checkSolving(Model model,
                               @PathVariable(name = "id") Test test,
                               @PathVariable(name = "id1") Student student,
                               @AuthenticationPrincipal Users user,
                               @RequestParam(name = "mark") Integer mark) {
        if (!auxiliaryService.security(user, test.getModule().getEducationGroup())) {
            return "error";
        }

        String solving = test.getStudentsSolving().get(student.getId());
        model.addAttribute("files", solving.split(" "));

        if (mark <= 1 || mark > 5) {
            model.addAttribute("info", student.getFullName());
            model.addAttribute("id", test.getId());
            model.addAttribute("id1", student.getId());
            model.addAttribute("message", "Оценка должна находиться в интервале [2; 5]");
            return "check";
        }

        teacherService.putMark(student, test, mark);

        return "redirect:/educated/test/" + test.getId() + "/statistics-with-files";
    }

    @GetMapping("/test/{id}/comments/{id1}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public String takeComments(Model model,
                               @PathVariable(name = "id") Test test,
                               @PathVariable(name = "id1") Student student) {
        model.addAttribute("test", test);
        model.addAttribute("student", student);
        return "take_coment";
    }

    @PostMapping("/test/{id}/comments/{id1}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public String takeComments(@PathVariable(name = "id1") Student student,
                               @PathVariable(name = "id") Test test,
                               @RequestParam(name = "comm") String comments,
                               @RequestParam(name = "theme") String theme) {
        teacherService.takeComments(student, theme, comments);
        if (!test.isIsFile()) {
            return "redirect:/educated/test/" + test.getId() + "/statistics";
        }
        return "redirect:/educated/test/" + test.getId() + "/statistics-with-files";
    }

    @GetMapping("/test/{id}/answers/{id1}")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'STUDENT')")
    public String studentsAnswers(Model model,
                                  @PathVariable(name = "id") Test test,
                                  @PathVariable(name = "id1") Student student,
                                  @AuthenticationPrincipal Users user) {

        String role = auxiliaryService.getRole(user);
        if (test.isControl() && role.equals("student")) {
            return "redirect:/educated/module/" + test.getModule().getId();
        }

        studentService.initTest(test);

        List<StudentsAnswers> answers = new ArrayList<>();
        for (Question question : test.getQuestions()) {
            for (StudentsAnswers studentsAnswers : question.getStudentsAnswers()) {
                if (studentsAnswers.getStudent().getId() == student.getId()) {
                    answers.add(studentsAnswers);
                }
            }
        }

        model.addAttribute("mark", test.getStudentsMarks().get(student.getId()));
        model.addAttribute("answers", answers);
        model.addAttribute("test", test);
        model.addAttribute("student", student);
        model.addAttribute("role", role);

        return "answers";
    }
}

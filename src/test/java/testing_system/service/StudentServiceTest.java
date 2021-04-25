package testing_system.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import testing_system.domain.group.EducationGroup;
import testing_system.domain.group.Module;
import testing_system.domain.people.Student;
import testing_system.domain.people.Teacher;
import testing_system.domain.test.Question;
import testing_system.repos.people.StudentRepo;
import testing_system.repos.test.TestRepo;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StudentServiceTest {
    @Autowired
    private StudentService studentService;
    @MockBean
    private MailSender mailSender;
    @MockBean
    private StudentRepo studentRepo;
    @MockBean
    private TestRepo testRepo;

    @Test
    public void doTest5() {
        Student student = getStudent("s@s.s");

        testing_system.domain.test.Test test = getAllTest(0, student);

        List<String> htmlA = new ArrayList<>(Arrays.asList(
                "1", "2", "3", "4", "5"
        ));

        int mark = studentService.doTest(test, htmlA, student);

        Assert.assertEquals(5, mark);
        assertForDoTest(test, student);
    }

    @Test
    public void doTest4() {
        Student student = getStudent("s@s.s");

        testing_system.domain.test.Test test = getAllTest(0, student);

        List<String> htmlA = new ArrayList<>(Arrays.asList(
                "1", "2", "3", "4", "6"
        ));

        int mark = studentService.doTest(test, htmlA, student);

        Assert.assertEquals(4, mark);
        assertForDoTest(test, student);

    }

    @Test
    public void doTest3() {
        Student student = getStudent("s@s.s");

        testing_system.domain.test.Test test = getAllTest(0, student);

        List<String> htmlA = new ArrayList<>(Arrays.asList(
                "1", "2", "3", "5", "4"
        ));

        int mark = studentService.doTest(test, htmlA, student);

        Assert.assertEquals(3, mark);
        assertForDoTest(test, student);

    }

    @Test
    public void doTest2() {
        Student student = getStudent("s@s.s");

        testing_system.domain.test.Test test = getAllTest(0, student);

        List<String> htmlA = new ArrayList<>(Arrays.asList(
                "5", "4", "3", "2", "1"
        ));

        int mark = studentService.doTest(test, htmlA, student);

        Assert.assertEquals(2, mark);
        assertForDoTest(test, student);

    }

    @Test
    public void createTestFromTicket() {
        testing_system.domain.test.Test test = getTest(5);
        test.setQuestions(getQuestions(25));

        Student student = new Student();
        student.setId(1);

        testing_system.domain.test.Test newTest = studentService.generateTestFromTicket(test, student);
        boolean checkQuestion = true;
        int index = 0;
        for (int i = 0; i < 25; i += 5) {
            List<Question> tempList = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                tempList.add(test.getQuestions().get(i+j));
            }
            if (!tempList.contains(test.getQuestions().get(index))) {
                checkQuestion = false;
                break;
            }
            index++;
        }

        Assert.assertEquals(5, newTest.getQuestions().size());
        Assert.assertTrue(checkQuestion);

    }

    private void assertForDoTest(testing_system.domain.test.Test test, Student student) {
        Mockito.verify(testRepo, Mockito.times(1))
                .save(test);

        Mockito.verify(studentRepo, Mockito.times(1))
                .save(student);

        Mockito.verify(mailSender, Mockito.times(2)).send(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()
        );
    }

    private Student getStudent(String username) {
        Student student = new Student();
        student.setUsername(username);
        student.setAllMarks(new HashMap<>());

        return student;
    }

    private testing_system.domain.test.Test getAllTest(int sections, Student student) {
        testing_system.domain.test.Test test = getTest(sections);

        EducationGroup educationGroup = new EducationGroup();
        educationGroup.setStudents(Collections.singletonList(student));
        educationGroup.setTeachers(Collections.singletonList(new Teacher("t@t.t")));

        Module module = new Module();

        module.setEducationGroup(educationGroup);
        test.setModule(module);

        List<Question> questions = getQuestions(5);
        test.setQuestions(questions);

        return test;
    }

    private testing_system.domain.test.Test getTest(int sections) {
        testing_system.domain.test.Test test = new testing_system.domain.test.Test();
        test.setSections(sections);
        Map<Integer, Integer> map = new HashMap<>();
        map.put(5, 100);
        map.put(4, 80);
        map.put(3, 60);
        test.setGradingSystem(map);
        test.setStudentsMarks(new HashMap<>());

        return test;
    }

    private List<Question> getQuestions(int n) {
        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < n; i ++) {
            Question question = new Question();
            question.setQuestion(1 + i + "");

            question.setAnswersOptions(new ArrayList<>(Collections.singletonList(i + 1 + "")));
            question.setCorrectAnswer(new ArrayList<>(Collections.singletonList(true)));

            questions.add(question);
        }

        return questions;
    }

}

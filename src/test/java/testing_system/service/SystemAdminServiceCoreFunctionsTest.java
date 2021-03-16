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
import testing_system.domain.message.Message;
import testing_system.domain.people.Student;
import testing_system.domain.people.Teacher;
import testing_system.repos.group.EducationGroupRepo;
import testing_system.repos.message.MessageRepo;
import testing_system.repos.people.StudentRepo;
import testing_system.repos.people.TeacherRepo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SystemAdminServiceCoreFunctionsTest {
    @Autowired
    private SystemAdminService systemAdminService;
    @MockBean
    private EducationGroupRepo educationGroupRepo;
    @MockBean
    private StudentRepo studentRepo;
    @MockBean
    private TeacherRepo teacherRepo;
    @MockBean
    private MailSender mailSender;
    @MockBean
    private MessageRepo messageRepo;

    @Test
    public void createEducationGroup() {
        Student student = getStudent("student@student.student");
        Student student1 = getStudent("student1@student1.student1");

        List<String> sEmails = new ArrayList<>(Arrays.asList(
                student.getUsername(),
                student1.getUsername()
        ));

        Teacher teacher = getTeacher("teacher@teacher.teacher");
        Teacher teacher1 = getTeacher("teacher1@teacher1.teacher1");

        List<String> tEmails = new ArrayList<>(Arrays.asList(
                teacher.getUsername(),
                teacher1.getUsername()
        ));

        String title = "Education group";

        boolean isCreated = systemAdminService.createEducationGroup(sEmails, tEmails, title);

        Assert.assertTrue(isCreated);
        Assert.assertNotNull(student.getGroups());
        Assert.assertNotNull(student1.getGroups());
        Assert.assertNotNull(teacher.getGroups());
        Assert.assertNotNull(teacher1.getGroups());

        Mockito.verify(studentRepo, Mockito.times(1))
                .save(student);
        Mockito.verify(studentRepo, Mockito.times(1))
                .save(student1);
        Mockito.verify(teacherRepo, Mockito.times(1))
                .save(teacher);
        Mockito.verify(teacherRepo, Mockito.times(1))
                .save(teacher1);
        Mockito.verify(educationGroupRepo, Mockito.times(1))
                .save(ArgumentMatchers.any(EducationGroup.class));
    }

    @Test
    public void createEducationGroupNoStudentFailTest() {
        Student student = getStudent("student@student.student");
        Student student1 = getStudent("student1@student1.student1");

        Mockito.doReturn(null)
                .when(studentRepo)
                .findByUsername(student.getUsername());
        Mockito.doReturn(null)
                .when(studentRepo)
                .findByUsername(student1.getUsername());

        List<String> sEmails = new ArrayList<>(Arrays.asList(
                student.getUsername(),
                student1.getUsername()
        ));

        Teacher teacher = getTeacher("teacher@teacher.teacher");
        Teacher teacher1 = getTeacher("teacher1@teacher1.teacher1");

        List<String> tEmails = new ArrayList<>(Arrays.asList(
                teacher.getUsername(),
                teacher1.getUsername()
        ));

        String title = "Education group";

        boolean isCreated = systemAdminService.createEducationGroup(sEmails, tEmails, title);

        assertForFailGroup(isCreated, student, student1, teacher, teacher1);
    }

    @Test
    public void createEducationGroupNoTeacherFailTest() {
        Student student = getStudent("student@student.student");
        Student student1 = getStudent("student1@student1.student1");

        List<String> sEmails = new ArrayList<>(Arrays.asList(
                student.getUsername(),
                student1.getUsername()
        ));

        Teacher teacher = getTeacher("teacher@teacher.teacher");
        Teacher teacher1 = getTeacher("teacher1@teacher1.teacher1");

        Mockito.doReturn(null)
                .when(teacherRepo)
                .findByUsername(teacher.getUsername());
        Mockito.doReturn(null)
                .when(teacherRepo)
                .findByUsername(teacher1.getUsername());

        List<String> tEmails = new ArrayList<>(Arrays.asList(
                teacher.getUsername(),
                teacher1.getUsername()
        ));

        String title = "Education group";

        boolean isCreated = systemAdminService.createEducationGroup(sEmails, tEmails, title);

        assertForFailGroup(isCreated, student, student1, teacher, teacher1);

    }

    @Test
    public void updateEducationGroupNoStudentFailTest() {
        Student student = getStudent("student@student.student");
        Student student1 = getStudent("student1@student1.student1");

        Mockito.doReturn(null)
                .when(studentRepo)
                .findByUsername(student.getUsername());
        Mockito.doReturn(null)
                .when(studentRepo)
                .findByUsername(student1.getUsername());

        List<String> sEmails = new ArrayList<>(Arrays.asList(
                student.getUsername(),
                student1.getUsername()
        ));

        Teacher teacher = getTeacher("teacher@teacher.teacher");
        Teacher teacher1 = getTeacher("teacher1@teacher1.teacher1");

        List<String> tEmails = new ArrayList<>(Arrays.asList(
                teacher.getUsername(),
                teacher1.getUsername()
        ));

        EducationGroup educationGroup = new EducationGroup();
        educationGroup.setStudents(new ArrayList<>());
        educationGroup.setTeachers(new ArrayList<>());
        String title = "Education group";

        boolean isCreated = systemAdminService.updateEducationGroup(sEmails, tEmails, title, educationGroup);

        assertForFailGroup(isCreated, student, student1, teacher, teacher1);
    }

    @Test
    public void updateEducationGroupNoTeacherFailTest() {
        Student student = getStudent("student@student.student");
        Student student1 = getStudent("student1@student1.student1");

        List<String> sEmails = new ArrayList<>(Arrays.asList(
                student.getUsername(),
                student1.getUsername()
        ));

        Teacher teacher = getTeacher("teacher@teacher.teacher");
        Teacher teacher1 = getTeacher("teacher1@teacher1.teacher1");

        Mockito.doReturn(null)
                .when(teacherRepo)
                .findByUsername(teacher.getUsername());
        Mockito.doReturn(null)
                .when(teacherRepo)
                .findByUsername(teacher1.getUsername());

        List<String> tEmails = new ArrayList<>(Arrays.asList(
                teacher.getUsername(),
                teacher1.getUsername()
        ));

        EducationGroup educationGroup = new EducationGroup();
        educationGroup.setStudents(new ArrayList<>());
        educationGroup.setTeachers(new ArrayList<>());
        String title = "Education group";

        boolean isCreated = systemAdminService.updateEducationGroup(sEmails, tEmails, title, educationGroup);

        assertForFailGroup(isCreated, student, student1, teacher, teacher1);


    }

    @Test
    public void answerTheQuestion() {
        Message message = new Message(237649, "theme", "text", "userEmail", "username");
        String answer = "HsidvnisipcsNlisb nsdua shi";

        systemAdminService.answerTheQuestion(message, answer);

        Mockito.verify(mailSender, Mockito.times(1))
                .send(
                        ArgumentMatchers.eq(message.getTheme()),
                        ArgumentMatchers.eq(message.getUserEmail()),
                        ArgumentMatchers.anyString()
                );

        Mockito.verify(messageRepo, Mockito.times(1))
                .deleteById(237649L);

    }

    // Создание студента через почту
    private Student getStudent(String username) {
        Student student = new Student();
        student.setGroups(new ArrayList<>());
        student.setUsername(username);
        Mockito.doReturn(student)
                .when(studentRepo)
                .findByUsername(student.getUsername());
        return student;
    }

    // Создание учителя через почту
    private Teacher getTeacher(String username) {
        Teacher teacher = new Teacher();
        teacher.setGroups(new ArrayList<>());
        teacher.setUsername(username);
        Mockito.doReturn(teacher)
                .when(teacherRepo)
                .findByUsername(teacher.getUsername());
        return teacher;
    }

    // Отрицательный тесты для создания/обновления групп
    private void assertForFailGroup(boolean isCreated, Student student, Student student1,
                                    Teacher teacher, Teacher teacher1) {
        Assert.assertFalse(isCreated);
        Assert.assertEquals(0, student.getGroups().size());
        Assert.assertEquals(0, student1.getGroups().size());
        Assert.assertEquals(0, teacher.getGroups().size());
        Assert.assertEquals(0, teacher1.getGroups().size());

        Mockito.verify(studentRepo, Mockito.times(0))
                .save(student);
        Mockito.verify(studentRepo, Mockito.times(0))
                .save(student1);
        Mockito.verify(teacherRepo, Mockito.times(0))
                .save(teacher);
        Mockito.verify(teacherRepo, Mockito.times(0))
                .save(teacher1);
        Mockito.verify(educationGroupRepo, Mockito.times(0))
                .save(ArgumentMatchers.any(EducationGroup.class));

    }

}

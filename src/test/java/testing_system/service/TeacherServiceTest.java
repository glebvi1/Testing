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
import testing_system.domain.test.Question;
import testing_system.repos.group.EducationGroupRepo;
import testing_system.repos.group.ModuleRepo;
import testing_system.repos.test.QuestionRepo;
import testing_system.repos.test.TestRepo;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TeacherServiceTest {
    @Autowired
    private TeacherService teacherService;
    @MockBean
    private ModuleRepo moduleRepo;
    @MockBean
    private EducationGroupRepo educationGroupRepo;
    @MockBean
    private TestRepo testRepo;
    @MockBean
    private QuestionRepo questionRepo;

    @Test
    public void addModule() {
        EducationGroup educationGroup = new EducationGroup();
        educationGroup.setModules(new HashSet<>());
        boolean isModule = teacherService.addModule(educationGroup, "Module");

        Assert.assertTrue(isModule);
        Mockito.verify(moduleRepo, Mockito.times(1))
                .save(ArgumentMatchers.any(Module.class));

        Mockito.verify(educationGroupRepo, Mockito
                .times(1)).save(educationGroup);

        Assert.assertEquals(1, educationGroup.getModules().size());
    }

    @Test
    public void addModuleFailTest() {
        EducationGroup educationGroup = new EducationGroup();
        educationGroup.setModules(Collections.singleton(new Module("Module")));

        boolean isModule = teacherService.addModule(educationGroup, "Module");

        Assert.assertFalse(isModule);
        Mockito.verify(moduleRepo, Mockito.times(0))
                .save(ArgumentMatchers.any(Module.class));

        Mockito.verify(educationGroupRepo, Mockito.times(0))
                .save(ArgumentMatchers.any(EducationGroup.class));

        Assert.assertEquals(1, educationGroup.getModules().size());

    }

    @Test
    public void addTest() {
        List<String> htmlQ = new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5", "6", "7"));
        List<String> htmlAO = new ArrayList<>(Arrays.asList(
                "1", "", "", "", "", "",
                "2", "", "", "", "", "",
                "3", "", "", "", "", "",
                "4", "", "", "", "", "",
                "5", "", "", "", "", "",
                "6", "", "", "", "", "",
                "7", "", "", "", "", ""));
        List<String> htmlCA = new ArrayList<>(Arrays.asList(
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off"
        ));
        Module module = new Module();
        module.setTests(new HashSet<>());
        List<Integer> marks = new ArrayList<>(Arrays.asList(100, 80, 60));

        teacherService.addTest(
                "Test", htmlQ, htmlAO, htmlCA, module, marks, 0,
                new ArrayList<>());

        Mockito.verify(questionRepo, Mockito.times(14))
                .save(ArgumentMatchers.any(Question.class));

        Mockito.verify(moduleRepo, Mockito.times(1))
                .save(ArgumentMatchers.any(Module.class));

        Mockito.verify(testRepo, Mockito.times(1))
                .save(ArgumentMatchers.any(testing_system.domain.test.Test.class));

    }

    @Test
    public void addTicket() {
        List<String> htmlQ = new ArrayList<>(Arrays.asList(
                "11", "12",
                "21", "22",
                "31", "32",
                "41", "42",
                "51", "52"));
        List<String> htmlAO = new ArrayList<>(Arrays.asList(
                "11", "", "", "", "", "",
                "12", "", "", "", "", "",
                "21", "", "", "", "", "",
                "22", "", "", "", "", "",
                "31", "", "", "", "", "",
                "32", "", "", "", "", "",
                "41", "", "", "", "", "",
                "42", "", "", "", "", "",
                "51", "", "", "", "", "",
                "52", "", "", "", "", ""));
        List<String> htmlCA = new ArrayList<>(Arrays.asList(
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off"
        ));
        Module module = new Module();
        module.setTests(new HashSet<>());
        List<Integer> marks = new ArrayList<>(Arrays.asList(100, 80, 60));

        teacherService.addTest(
                "Test", htmlQ, htmlAO, htmlCA, module, marks, 5,
                new ArrayList<>());

        Mockito.verify(questionRepo, Mockito.times(20))
                .save(ArgumentMatchers.any(Question.class));

        Mockito.verify(moduleRepo, Mockito.times(1))
                .save(ArgumentMatchers.any(Module.class));

        Mockito.verify(testRepo, Mockito.times(1))
                .save(ArgumentMatchers.any(testing_system.domain.test.Test.class));

    }

    @Test
    public void addTicketTranspose() {
        List<String> htmlQ = new ArrayList<>(Arrays.asList(
                "11", "12", "13", "14", "15",
                "21", "22", "23", "24", "25"));
        List<String> htmlAO = new ArrayList<>(Arrays.asList(
                "11", "", "", "", "", "",
                "12", "", "", "", "", "",
                "13", "", "", "", "", "",
                "14", "", "", "", "", "",
                "15", "", "", "", "", "",
                "21", "", "", "", "", "",
                "22", "", "", "", "", "",
                "23", "", "", "", "", "",
                "24", "", "", "", "", "",
                "25", "", "", "", "", ""));
        List<String> htmlCA = new ArrayList<>(Arrays.asList(
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off",
                "on", "off", "off", "off", "off", "off", "off"
        ));
        Module module = new Module();
        module.setTests(new HashSet<>());
        List<Integer> marks = new ArrayList<>(Arrays.asList(100, 80, 60));

        teacherService.addTest(
                "Test", htmlQ, htmlAO, htmlCA, module, marks, 2,
                new ArrayList<>());

        Mockito.verify(questionRepo, Mockito.times(20))
                .save(ArgumentMatchers.any(Question.class));

        Mockito.verify(moduleRepo, Mockito.times(1))
                .save(ArgumentMatchers.any(Module.class));

        Mockito.verify(testRepo, Mockito.times(1))
                .save(ArgumentMatchers.any(testing_system.domain.test.Test.class));


    }

}

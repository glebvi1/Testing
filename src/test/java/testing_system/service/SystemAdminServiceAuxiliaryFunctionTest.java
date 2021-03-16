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
import testing_system.domain.people.Roles;
import testing_system.domain.people.Student;
import testing_system.domain.people.Teacher;
import testing_system.domain.people.User;
import testing_system.repos.people.StudentRepo;
import testing_system.repos.people.TeacherRepo;
import testing_system.repos.people.UserRepo;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SystemAdminServiceAuxiliaryFunctionTest {
    @Autowired
    private SystemAdminService systemAdminService;
    @MockBean
    private UserRepo userRepo;
    @MockBean
    private TeacherRepo teacherRepo;
    @MockBean
    private StudentRepo studentRepo;

    @Test
    public void editStudentToTeacherAndAdmin() {
        User user = new User();
        Set<Roles> roles = new HashSet<>();
        roles.add(Roles.STUDENT);
        user.setRoles(roles);

        Map<String, String> htmlR = new HashMap<>();
        htmlR.put("TEACHER", "on");
        htmlR.put("TEACHER_ADMIN", "on");

        boolean isEdit = systemAdminService.editUser(user, htmlR);

        Assert.assertTrue(isEdit);
        Mockito.verify(teacherRepo, Mockito.times(1))
                .save(ArgumentMatchers.any(Teacher.class));
        Mockito.verify(userRepo, Mockito.times(1))
                .delete(user);
    }

    @Test
    public void editTeacherToStudent() {
        User user = new User();
        Set<Roles> roles = new HashSet<>();
        roles.add(Roles.TEACHER);
        user.setRoles(roles);

        Map<String, String> htmlR = new HashMap<>();
        htmlR.put("STUDENT", "on");

        boolean isEdit = systemAdminService.editUser(user, htmlR);

        Assert.assertTrue(isEdit);
        Mockito.verify(userRepo, Mockito.times(1))
                .delete(ArgumentMatchers.any(User.class));

        Mockito.verify(studentRepo, Mockito.times(1))
                .save(ArgumentMatchers.any(Student.class));

    }

    @Test
    public void editStudentToSysAdmin() {
        User user = new User();
        Set<Roles> roles = new HashSet<>();
        roles.add(Roles.STUDENT);
        user.setRoles(roles);

        Map<String, String> htmlR = new HashMap<>();
        htmlR.put("SYSTEM_ADMIN", "on");

        boolean isEdit = systemAdminService.editUser(user, htmlR);

        Assert.assertTrue(isEdit);
        Mockito.verify(userRepo, Mockito.times(1))
                .deleteById(ArgumentMatchers.any(Long.class));
        Mockito.verify(userRepo, Mockito.times(1))
                .save(ArgumentMatchers.any(User.class));
    }

    @Test
    public void editFailStudentAndTeacher() {
        User user = new User();
        Set<Roles> roles = new HashSet<>();
        roles.add(Roles.STUDENT);
        user.setRoles(roles);

        Map<String, String> htmlR = new HashMap<>();
        htmlR.put("STUDENT", "on");
        htmlR.put("TEACHER", "on");

        boolean isEdit = systemAdminService.editUser(user, htmlR);

        Assert.assertFalse(isEdit);
        Mockito.verify(teacherRepo, Mockito.times(0))
                .save(ArgumentMatchers.any(Teacher.class));

        Mockito.verify(userRepo, Mockito.times(0))
                .delete(user);
    }

    @Test
    public void editFailSysAdminAndStudent() {
        User user = new User();
        Set<Roles> roles = new HashSet<>();
        roles.add(Roles.SYSTEM_ADMIN);
        user.setRoles(roles);

        Map<String, String> htmlR = new HashMap<>();
        htmlR.put("STUDENT", "on");
        htmlR.put("SYSTEM_ADMIN", "on");

        boolean isEdit = systemAdminService.editUser(user, htmlR);

        Assert.assertFalse(isEdit);
        Mockito.verify(studentRepo, Mockito.times(0))
                .save(ArgumentMatchers.any(Student.class));

        Mockito.verify(userRepo, Mockito.times(0))
                .delete(user);
    }

    @Test
    public void sortByUsername() {
        String username = "a@a.a";
        String fullName = "";
        User user = new User();
        user.setRoles(new HashSet<Roles>());

        Mockito.doReturn(user)
                .when(userRepo)
                .findByUsername(username);

        List<User> users = systemAdminService.sort(username, fullName);

        Assert.assertNotNull(users);
        Mockito.verify(userRepo, Mockito.times(1))
                .findByUsername(username);
        Mockito.verify(userRepo, Mockito.times(0))
                .findAllByFullName(ArgumentMatchers.anyString());
        Mockito.verify(userRepo, Mockito.times(0))
                .findAllByUsernameAndFullName(ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
    }

    @Test
    public void sortByFullName() {
        String username = "";
        String fullName = "name";
        User user = new User();
        user.setRoles(new HashSet<Roles>());
        User user1 = new User();
        user1.setRoles(new HashSet<Roles>());

        Mockito.doReturn(new ArrayList<>(Arrays.asList(user, user1)))
                .when(userRepo)
                .findAllByFullName(fullName);

        List<User> users = systemAdminService.sort(username, fullName);

        Assert.assertEquals(2, users.size());
        Mockito.verify(userRepo, Mockito.times(0))
                .findByUsername(ArgumentMatchers.anyString());
        Mockito.verify(userRepo, Mockito.times(1))
                .findAllByFullName(fullName);
        Mockito.verify(userRepo, Mockito.times(0))
                .findAllByUsernameAndFullName(ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
    }

    @Test
    public void sortByUsernameAndFullName() {
        String username = "a@a.a";
        String fullName = "name";
        User user = new User();
        user.setRoles(new HashSet<Roles>());

        Mockito.doReturn(new ArrayList<>(Collections.singletonList(user)))
                .when(userRepo)
                .findAllByUsernameAndFullName(username, fullName);

        List<User> users = systemAdminService.sort(username, fullName);

        Assert.assertNotNull(users);
        Mockito.verify(userRepo, Mockito.times(0))
                .findByUsername(ArgumentMatchers.anyString());
        Mockito.verify(userRepo, Mockito.times(0))
                .findAllByFullName(ArgumentMatchers.anyString());
        Mockito.verify(userRepo, Mockito.times(1))
                .findAllByUsernameAndFullName(username, fullName);
    }

    @Test
    public void sortNull() {
        String username = "a@a.a";
        String fullName = "name";

        Mockito.doReturn(null)
                .when(userRepo)
                .findAllByUsernameAndFullName(username, fullName);

        List<User> users = systemAdminService.sort(username, fullName);

        Assert.assertNull(users);
        Mockito.verify(userRepo, Mockito.times(0))
                .findByUsername(ArgumentMatchers.anyString());
        Mockito.verify(userRepo, Mockito.times(0))
                .findAllByFullName(ArgumentMatchers.anyString());
        Mockito.verify(userRepo, Mockito.times(1))
                .findAllByUsernameAndFullName(username, fullName);
    }

}

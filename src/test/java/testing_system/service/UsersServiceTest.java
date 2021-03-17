package testing_system.service;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import testing_system.domain.people.Users;
import testing_system.domain.people.Roles;
import testing_system.domain.people.Student;
import testing_system.repos.people.StudentRepo;
import testing_system.repos.people.UserRepo;

import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UsersServiceTest {
    @Autowired
    private UserService userService;
    @MockBean
    private StudentRepo studentRepo;
    @MockBean
    private UserRepo userRepo;
    @MockBean
    private MailSender mailSender;
    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    public void addUser() {
        Student student = new Student();
        student.setUsername("some@mail.ru");

        boolean isUserCreated = userService.addUser(student);

        Assert.assertTrue(isUserCreated);
        Assert.assertNotNull(student.getActivatedCode());
        Assert.assertTrue(CoreMatchers.is(student.getRoles())
                .matches(Collections.singleton(Roles.STUDENT)));

        Mockito.verify(studentRepo, Mockito.times(1)).save(student);
        Mockito.verify(mailSender, Mockito.times(1))
                .send(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(student.getUsername()),
                        ArgumentMatchers.anyString()
                );

    }

    @Test
    public void addUserFailTest() {
        Student student = new Student();

        student.setUsername("Глеб");

        Mockito.doReturn(new Student())
                .when(userRepo)
                .findByUsername("Глеб");

        boolean isUserCreated = userService.addUser(student);

        Assert.assertFalse(isUserCreated);

        Mockito.verify(studentRepo, Mockito.times(0))
                .save(ArgumentMatchers.any(Student.class));
        Mockito.verify(mailSender, Mockito.times(0))
                .send(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyString()
                );
    }

    @Test
    public void isActivated() {
        Users user = new Users();

        user.setActivatedCode("123");

        Mockito.doReturn(user)
                .when(userRepo)
                .findByActivatedCode("456");

        boolean isUserActivated = userService.isActivated("456");

        Assert.assertTrue(isUserActivated);
        Assert.assertNull(user.getActivatedCode());

        Mockito.verify(userRepo, Mockito.times(1)).save(user);
    }

    @Test
    public void activateUserFailTest() {
        boolean isUserActivated = userService.isActivated("123");
        Assert.assertFalse(isUserActivated);
        Mockito.verify(userRepo, Mockito.times(0))
                .save(ArgumentMatchers.any(Users.class));
    }

    @Test
    public void updateUser() {
        Users user = new Users();
        user.setPassword("123");

        boolean isUpdate = userService.updateUser(user, "new name", "new email", "new password", user.getPassword());

        Assert.assertEquals("new email", user.getUsername());
        Assert.assertEquals("new name", user.getFullName());
        Assert.assertEquals("new password", user.getPassword());
        Assert.assertTrue(isUpdate);
        Mockito.verify(userRepo, Mockito.times(1)).save(user);

    }

    @Test
    public void updateUserFailTest() {
        Users user = new Users();
        user.setFullName("name");
        user.setUsername("email");
        user.setPassword("123");

        boolean isUpdate = userService.updateUser(user, "new name", "new email", "new password", "456");

        Assert.assertFalse(isUpdate);
        Assert.assertEquals("email", user.getUsername());
        Assert.assertEquals("name", user.getFullName());
        Assert.assertEquals("123", user.getPassword());
        Mockito.verify(userRepo, Mockito.times(0))
                .save(ArgumentMatchers.any(Users.class));
    }
}

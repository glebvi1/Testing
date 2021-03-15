package testing_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import testing_system.domain.people.Roles;
import testing_system.domain.people.Student;
import testing_system.domain.people.User;
import testing_system.repos.people.StudentRepo;
import testing_system.repos.people.UserRepo;

import java.util.Collections;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private MailSender mailSender;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username);
        if (user == null || user.getActivatedCode() != null) {
            throw new UsernameNotFoundException(
                    "No user found with username: " + username);
        }
        return user;
    }

    public boolean updateUser(User user, String newName, String newEmail, String newPassword, String confirmPassword) {
        if (!user.getPassword().equals(confirmPassword)) {
            return false;
        }

        user.setFullName(newName);
        user.setUsername(newEmail);
        user.setPassword(newPassword);
        userRepo.save(user);

        return true;
    }

    public boolean addUser(Student user) {
        User userFromDb = userRepo.findByUsername(user.getUsername());

        if (userFromDb != null) {
            return false;
        }

        user.setFullName(user.getFullName());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(Roles.STUDENT));
        user.setActivatedCode(UUID.randomUUID().toString());

        String message = String.format(
                "Уважаемый %s, пожалуйста, перейдите по ссылке для активации аккаунта.\n"+
                "http://localhost:8080/activate/%s",
                user.getFullName(),
                user.getActivatedCode()
        );

        // Почты не существует
        try {
            mailSender.send("Код активации", user.getUsername(), message);
        } catch (MailSendException m) {
            return false;
        }
        studentRepo.save(user);

        return true;
    }

    public boolean isActivated(String code) {
        User userFromDb = userRepo.findByActivatedCode(code);
        if (userFromDb == null) {
            return false;
        }

        userFromDb.setActivatedCode(null);
        userRepo.save(userFromDb);

        return true;
    }

}

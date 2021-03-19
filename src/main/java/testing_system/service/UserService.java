package testing_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import testing_system.domain.people.Roles;
import testing_system.domain.people.Student;
import testing_system.domain.people.Users;
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
        Users user = userRepo.findByUsername(username);
        if (user == null || user.getActivatedCode() != null) {
            throw new UsernameNotFoundException(
                    "No user found with username: " + username);
        }
        return user;
    }

    // Изменение данных пользователя
    public boolean updateUser(Users user, String newName, String newEmail, String newPassword, String confirmPassword) {
        if (!passwordEncoder.matches(confirmPassword, user.getPassword())) {
            return false;
        }

        boolean isEmailChanged = (newEmail != null && !newEmail.equals(user.getUsername())) ||
                (user != null && !user.equals(newEmail));

        if (isEmailChanged) {
            Users userFromDb = userRepo.findByUsername(newEmail);
            if (userFromDb != null) {
                return false;
            }
            user.setUsername(newEmail);
            if (sendActivationCode(user)) return false;
            user.setActivatedCode(UUID.randomUUID().toString());
        }

        if (!StringUtils.isEmpty(newName)) {
            user.setFullName(newName);
        }
        if (StringUtils.isEmpty(newPassword) ) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepo.save(user);

        return true;
    }

    // Сохранение пользователя в БД
    // Отправление активационного кода на указанную почту
    public boolean addUser(Student user) {
        Users userFromDb = userRepo.findByUsername(user.getUsername());

        if (userFromDb != null) {
            return false;
        }

        user.setFullName(user.getFullName());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(Roles.STUDENT));
        user.setActivatedCode(UUID.randomUUID().toString());

        if (sendActivationCode(user)) return false;
        studentRepo.save(user);

        return true;
    }

    private boolean sendActivationCode(Users user) {
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
            return true;
        }
        return false;
    }

    // Успешно ли прошла активация
    public boolean isActivated(String code) {
        Users userFromDb = userRepo.findByActivatedCode(code);
        if (userFromDb == null) {
            return false;
        }

        userFromDb.setActivatedCode(null);
        userRepo.save(userFromDb);

        return true;
    }

}

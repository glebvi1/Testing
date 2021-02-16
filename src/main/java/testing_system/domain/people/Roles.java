package testing_system.domain.people;

import org.springframework.security.core.GrantedAuthority;

public enum Roles implements GrantedAuthority {
    STUDENT,
    TEACHER,
    TEACHER_ADMIN,
    SYSTEM_ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }
}

package testing_system.domain.people;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;

@Entity
@Table
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String fullName;
    private String username;
    private String password;
    private String activatedCode;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = Roles.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    private Set<Roles> roles;

    public User() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setUsername(String email) {
        this.username = email;
    }

    public Set<Roles> getRoles() {
        return roles;
    }

    public void setRoles(Set<Roles> roles) {
        this.roles = roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isTeacher() {
        return this.roles.contains(Roles.TEACHER);
    }

    public String getActivatedCode() {
        return activatedCode;
    }

    public void setActivatedCode(String activatedCode) {
        this.activatedCode = activatedCode;
    }

    public String getStringRoles() {
        String roles = "";
        boolean isStudent = this.getRoles().contains(Roles.STUDENT);
        boolean isAdmin = this.getRoles().contains(Roles.TEACHER_ADMIN);
        if (isStudent) {
            roles += "студент";
        }
        int size = this.getRoles().size();
        if (size > 1 && isStudent) {
            roles += " ";
        }
        if (this.getRoles().contains(Roles.TEACHER)) {
            roles += "учитель";
        }

        if (isAdmin) {
            if (roles.length() != 0) {
                roles += " ";
            }
            roles += "администратор";
        }

        return roles.replaceAll(" ", ", ");
    }
}

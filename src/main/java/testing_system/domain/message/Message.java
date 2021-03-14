package testing_system.domain.message;

import javax.persistence.*;

@Entity
@Table
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(length = 100)
    private String theme;
    @Column(length = 500)
    private String text;
    private String userEmail;
    private String userName;

    public Message(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

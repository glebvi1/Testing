package testing_system.domain.test;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.List;

@Entity
@Table
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String question;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> answersOptions;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Boolean> correctAnswer;

    public Question() {}

    public Question(String question) {
        this.question = question;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getAnswersOptions() {
        return answersOptions;
    }

    public void setAnswersOptions(List<String> answersOptions) {
        this.answersOptions = answersOptions;
    }

    public List<Boolean> getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(List<Boolean> correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Question) {
            return id == ((Question) obj).getId();
        }
        return false;
    }
}

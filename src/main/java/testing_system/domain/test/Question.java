package testing_system.domain.test;

import javax.persistence.*;
import java.util.List;

@Entity
@Table
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String question;

    @ElementCollection
    private List<String> answersOptions;

    @ElementCollection
    private List<Boolean> correctAnswer;

    public Question() {}

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
}

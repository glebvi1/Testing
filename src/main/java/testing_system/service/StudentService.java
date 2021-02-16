package testing_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import testing_system.domain.people.Student;
import testing_system.domain.test.Question;
import testing_system.domain.test.Test;
import testing_system.repos.people.StudentRepo;
import testing_system.repos.test.TestRepo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private TestRepo testRepo;

    public int doTest(Test test, List<String> htmlAnswers, Student student) {

        int countOfCorrectAnswers = 0;
        int index = 0;

        for (Question question : test.getQuestions()) {
            List<String> answersOptions = question.getAnswersOptions();
            if (answersOptions.size() == 1) {
                if (answersOptions.get(0).equals(htmlAnswers.get(index))) {
                    countOfCorrectAnswers++;
                }
                index++;
            } else {
                List<Boolean> correctAnswers = question.getCorrectAnswer();
                int count = 0;
                for (int i = 0; i < answersOptions.size(); i++) {
                    if (correctAnswers.get(i) && htmlAnswers.get(index).equals("on")) {
                        count++;
                        htmlAnswers.remove(index + 1);
                    }
                    if (!correctAnswers.get(i) && htmlAnswers.get(index).equals("off")) {
                        count++;
                    }
                    index++;
                }
                if (count == answersOptions.size()) {
                    countOfCorrectAnswers++;
                }
            }

        }

        int mark = countOfCorrectAnswers / test.getQuestions().size() * 100;
        Map<Integer, Integer> marks = test.getGradingSystem();

        int five = marks.get(5);
        if (mark >= five) {
            mark = 5;
        } else {
            int four = marks.get(4);
            if (mark >= four) {
                mark = 4;
            } else {
                int three = marks.get(3);
                if (three >= mark) {
                    mark = 3;
                } else {
                    mark = 2;
                }
            }
        }

        Map<Long, Integer> studentsMarks = test.getStudentsMarks();
        if (studentsMarks == null) {
            studentsMarks = new HashMap<>();
        }
        studentsMarks.put(student.getId(), mark);
        testRepo.save(test);

        Map<Long, Integer> allMarks = student.getAllMarks();
        if (allMarks == null) {
            allMarks = new HashMap<>();
        }
        allMarks.put(test.getId(), mark);
        studentRepo.save(student);

        return mark;
    }

}

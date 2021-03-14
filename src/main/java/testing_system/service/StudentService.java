package testing_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import testing_system.domain.group.EducationGroup;
import testing_system.domain.people.Student;
import testing_system.domain.people.Teacher;
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
    @Autowired
    private MailSender mailSender;

    // Выставляется оценка за тест, отправляется письмо студентам и учителям
    public int doTest(Test test, List<String> htmlAnswers, Student student) {

        int countOfCorrectAnswers = parseHtml(test.getQuestions(), htmlAnswers);

        Map<Integer, Integer> marks = test.getGradingSystem();
        int mark = putMark(marks,countOfCorrectAnswers / test.getQuestions().size() * 100);

        // Сохранение оценки в БД
        Map<Long, Integer> studentsMarks = test.getStudentsMarks();
        Map<Long, Integer> allMarks = student.getAllMarks();

        if (studentsMarks == null) {
            studentsMarks = new HashMap<>();
            allMarks = new HashMap<>();
        }
        studentsMarks.put(student.getId(), mark);
        testRepo.save(test);

        allMarks.put(test.getId(), mark);
        studentRepo.save(student);

        // Отправка письма студенту, о прохождении теста
        String mes = String.format(
                "Уважаемый, %s!\n"+
                        "Вы завершили прохождения теста %s на оценку %s",
                student.getFullName(),
                test.getTitle(),
                mark
        );
        mailSender.send("Прохождение теста", student.getUsername(), mes);

        // Отправка письма учителю (только в то случае, если все студенты прошли данный тест)
        EducationGroup group = test.getModule().getEducationGroup();
        if (group.getStudents().size() == test.getStudentsMarks().size()) {
            String url = "http://localhost:8080/educated/test/" + test.getId() + "/statistics";
            for (Teacher teacher : group.getTeachers()) {
                mes = String.format(
                        "Уважаемый, %s"+
                                "Тест %s выполнен всеми студентами. Результаты Вы можете посмотреть на %s",
                        teacher.getFullName(),
                        test.getTitle(),
                        url
                );
                mailSender.send("Прохождение теста", teacher.getUsername(), mes);
            }
        }

        return mark;
    }

    // Возращает оценку за тест
    private int putMark(Map<Integer, Integer> marks, double currentMark) {

        int five = marks.get(5);
        if (currentMark >= five) {
            return 5;
        } else {
            int four = marks.get(4);
            if (currentMark >= four) {
                return 4;
            } else {
                int three = marks.get(3);
                if (currentMark >= three) {
                    return 3;
                }
                return 2;
            }
        }
    }

    // Парсинг html (данных, полученных с клиента)
    private int parseHtml(List<Question> questions, List<String> htmlAnswers) {
        int countOfCorrectAnswers = 0;
        int index = 0;

        for (Question question : questions) {
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

        return countOfCorrectAnswers;
    }

}

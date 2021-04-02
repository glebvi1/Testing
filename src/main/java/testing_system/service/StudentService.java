package testing_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import testing_system.domain.group.EducationGroup;
import testing_system.domain.people.Student;
import testing_system.domain.people.Teacher;
import testing_system.domain.test.Question;
import testing_system.domain.test.Test;
import testing_system.repos.people.StudentRepo;
import testing_system.repos.test.TestRepo;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class StudentService {
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private TestRepo testRepo;
    @Autowired
    private MailSender mailSender;

    @Value("${upload.path}")
    private String uploadPath;

    // Выставляется оценка за тест, отправляется письмо студентам и учителям
    public int doTest(Test test, List<String> htmlAnswers, Student student) {
        int countOfCorrectAnswers = parseHtml(test.getQuestions(), htmlAnswers);

        Map<Integer, Integer> marks = test.getGradingSystem();

        int mark = putMark(marks,(float) countOfCorrectAnswers / (float) test.getQuestions().size());

        // Сохранение оценки в БД
        Map<Long, Integer> studentsMarks = test.getStudentsMarks();
        Map<Long, Integer> allMarks = student.getAllMarks();

        if (studentsMarks == null) {
            studentsMarks = new HashMap<>();
        }
        if (allMarks == null) {
            allMarks = new HashMap<>();
        }
        studentsMarks.put(student.getId(), mark);
        testRepo.save(test);

        allMarks.put(test.getId(), mark);
        studentRepo.save(student);

        // Отправка письма студенту, о прохождении теста
        sendToStudent(test, student, mark);

        // Отправка письма учителю (только в то случае, если все студенты прошли данный тест)
        sendToTeacher(test);
        return mark;
    }

    // Выставляется оценка за билет, отправляется письмо студентам и учителям
    public int doTicket(Test test, Test ticket, List<String> htmlAnswers,
                        Student student) {
        // Кол-во правильных ответов
        int countOfCorrectAnswers = parseHtml(test.getQuestions(), htmlAnswers);

        // Система оценивания. Берем из БИЛЕТА!!!
        Map<Integer, Integer> marks = ticket.getGradingSystem();

        // Оценка за тест
        int mark = putMark(marks,(float) countOfCorrectAnswers / (float) test.getQuestions().size());

        // Сохранение оценки в БД
        Map<Long, Integer> studentsMarks = ticket.getStudentsMarks();
        Map<Long, Integer> allMarks = student.getAllMarks();

        if (studentsMarks == null) {
            studentsMarks = new HashMap<>();
        }
        if (allMarks == null) {
            allMarks = new HashMap<>();
        }

        // Сохранение БИЛЕТА, а не конкретного теста
        studentsMarks.put(student.getId(), mark);
        ticket.setStudentsMarks(studentsMarks);
        testRepo.save(ticket);

        // Созранение пользователя с оценкой
        allMarks.put(test.getId(), mark);
        student.setAllMarks(allMarks);
        studentRepo.save(student);

        // Отправка письма студенту, о прохождении теста
        sendToStudent(ticket, student, mark);

        // Отправка письма учителю (только в то случае, если все студенты прошли данный тест)
        sendToTeacher(ticket);
        return mark;

    }

    // Сохраняются файлы, прикрепленные учеником
    public void doTestWithFile(Test test, List<MultipartFile> files, Student student) throws IOException {
        Map<Long, String> solving = new HashMap<>();
        String sol = "";

        // Сохраняем в папку файлы
        for (MultipartFile file : files) {
            if (file != null && !file.getOriginalFilename().isEmpty()) {
                String uuidFile = UUID.randomUUID().toString();
                String resultFilename = uuidFile + "." + file.getOriginalFilename();

                file.transferTo(new File(uploadPath + "/" + resultFilename));

                sol += resultFilename + " ";
            }
        }

        sol = sol.substring(0, sol.length() - 1);
        solving.put(student.getId(), sol);

        test.setStudentsSolving(solving);
        test.setStudentsMarks(new HashMap<>());

        testRepo.save(test);
    }

    // Состовляем из билета тест (рандомно выбираем вопросы из разделов)
    public Test generateTestFromTicket(Test test) {
        Test newTest = new Test();
        newTest.setQuestions(new ArrayList<Question>());
        newTest.setSections(test.getSections());
        newTest.setTitle(test.getTitle());
        newTest.setId(test.getId());

        int n = test.getQuestions().size();
        int section = n / test.getSections();

        for (int i = 0; i < n; i += section) {
            List<Question> tempQuestions = new ArrayList<>();

            for (int j = 0; j < section && (i+j) < n; j++) {
                tempQuestions.add(test.getQuestions().get(i + j));
            }

            int rnd = section - 1;
            int randomQ = (int)(Math.random() * ++rnd);
            newTest.getQuestions().add(tempQuestions.get(randomQ));
        }

        return newTest;
    }

    public List<Question> initTest(Test test) {
        List<Question> questions = test.getQuestions();
        List<Question> news = new ArrayList<>();
        for (Question question : questions) {
            if (!news.contains(question)) {
                news.add(question);
            }
        }
        test.setQuestions(news);

        for (Question question : test.getQuestions()) {
            List<String> ao = new ArrayList<>();

            for (String answer : question.getAnswersOptions()) {
                if (!ao.contains(answer)) {
                    ao.add(answer);
                }
            }

            question.setAnswersOptions(ao);
        }
        return news;
    }

    // Отправление письма ученику
    private void sendToStudent(Test test, Student student, int mark) {
        String mes = String.format(
                "Уважаемый, %s!\n" +
                        "Вы завершили прохождения теста \"%s\" на оценку %s",
                student.getFullName(),
                test.getTitle(),
                mark
        );
        mailSender.send("Прохождение теста", student.getUsername(), mes);
    }

    // Отправление письма учиелю
    private void sendToTeacher(Test test) {
        String mes;
        EducationGroup group = test.getModule().getEducationGroup();
        int size = group.getStudents().size();
        if (size == test.getStudentsMarks().size()) {
            String url = "http://localhost:8080/educated/test/" + test.getId() + "/statistics";
            for (Teacher teacher : group.getTeachers()) {
                mes = String.format(
                        "Уважаемый %s" +
                                "Тест %s выполнен всеми студентами. Результаты Вы можете посмотреть на %s",
                        teacher.getFullName(),
                        test.getTitle(),
                        url
                );
                mailSender.send("Прохождение теста", teacher.getUsername(), mes);
            }
        }
    }

    // Возращает оценку за тест
    private int putMark(Map<Integer, Integer> marks, double currentMark) {
        float five = (float) marks.get(5) / 100;
        if (currentMark >= five) {
            return 5;
        } else {
            float four = (float) marks.get(4) / 100;
            if (currentMark >= four) {
                return 4;
            } else {
                float three = (float) marks.get(3) / 100;
                if (currentMark >= three) {
                    return 3;
                }
                return 2;
            }
        }
    }

    // Парсинг html (данных, полученных с клиента)
    // Подсчет правильных ответов
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

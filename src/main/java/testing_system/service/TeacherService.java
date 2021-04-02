package testing_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import testing_system.domain.group.EducationGroup;
import testing_system.domain.group.Module;
import testing_system.domain.people.Student;
import testing_system.domain.test.Question;
import testing_system.domain.test.Test;
import testing_system.repos.group.EducationGroupRepo;
import testing_system.repos.group.ModuleRepo;
import testing_system.repos.people.StudentRepo;
import testing_system.repos.test.QuestionRepo;
import testing_system.repos.test.TestRepo;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherService {
    @Autowired
    private QuestionRepo questionRepo;
    @Autowired
    private TestRepo testRepo;
    @Autowired
    private ModuleRepo moduleRepo;
    @Autowired
    private EducationGroupRepo educationGroupRepo;
    @Autowired
    private StudentRepo studentRepo;

    @Value("${upload.path}")
    private String uploadPath;

    // Создание ногово теста
    public void addTest(String title, List<String> htmlQuestions, List<String> htmlAnswersOptions,
                        List<String> htmlCorrectAnswers, Module module, List<Integer> marks, int section) {
        List<Boolean> correctAnswers = parseHtmlCheckbox(htmlCorrectAnswers);
        List<Question> questions = parseHtmlQuestions(htmlQuestions, correctAnswers, htmlAnswersOptions);

        Map<Integer, Integer> gradingSystem = new HashMap<>(marks.size());

        int k = 5;
        for (int i = 0; i < 3; i++) {
            gradingSystem.put(k, marks.get(i));
            k--;
        }

        Test test = new Test();
        test.setTitle(title);

        shuffle(questions);

        test.setQuestions(questions);
        test.setGradingSystem(gradingSystem);
        test.setModule(module);
        test.setSections(section);
        testRepo.save(test);

        module.getTests().add(test);
        moduleRepo.save(module);
    }

    // Статистика успеваемости за тест
    public String[] statistics(List<Integer> allMarks) {
        String[] strings = new String[3];

        int maxMark = allMarks.get(0);
        int minMark = allMarks.get(0);
        float average = 0;

        for (int mark : allMarks) {
            if (maxMark < mark) {
                maxMark = mark;
            }
            if (minMark > mark) {
                minMark = mark;
            }
            average += mark;
        }
        average /= allMarks.size();
        strings[0] = maxMark+"";
        strings[1] = minMark+"";

        String[] sAverage = (average+"").split("\\.");
        if (sAverage[1].length() >= 2) {
            strings[2] = sAverage[0] + "." + sAverage[1].charAt(0) + sAverage[1].charAt(1);
        } else {
            strings[2] = average+"";
        }

        return strings;
    }

    // Создание нового учебного модуля
    public boolean addModule(EducationGroup educationGroup, String title) {
        Set<Module> allModules = educationGroup.getModules();
        // module.title - уникален
        if (allModules != null) {
            for (Module module : allModules) {
                if (module.getTitle().equals(title)) {
                    return false;
                }
            }
        }

        // Сохранение модуля в БД
        Module module = new Module();
        module.setTitle(title);
        module.setEducationGroup(educationGroup);
        moduleRepo.save(module);

        educationGroup.getModules().add(module);
        educationGroupRepo.save(educationGroup);

        return true;
    }

    // Правильные варианты ответа из списка вопросов
    public List<String> getCorrectAnswers(List<Question> questions) {
        List<String> answers = new ArrayList<>();

        for (Question question : questions) {
            List<String> answersOptions = question.getAnswersOptions();
            if (answersOptions.size() == 1) {
                answers.add(answersOptions.get(0));
            } else {
                List<Boolean> correctAnswers = question.getCorrectAnswer();
                String cor = "";

                for (int i = 0; i < answersOptions.size(); i++) {
                    if (correctAnswers.get(i)) {
                        cor += answersOptions.get(i) + ", ";
                    }
                }
                answers.add(cor.substring(0, cor.length() - 2));
            }
        }

        return answers;
    }

    // Создание нового теста с прикрепленным файлом
    public void addTestWithFile(MultipartFile file, String title, String question,
                                Module module) throws IOException {
        Test test = new Test();

        if (file != null && !file.getOriginalFilename().isEmpty()) {
            File uploadDir = new File(uploadPath);

            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            String uuidFile = UUID.randomUUID().toString();
            String resultFilename = uuidFile + "." + file.getOriginalFilename();

            file.transferTo(new File(uploadPath + "/" + resultFilename));
            test.setFilename(resultFilename);
        }

        Question question1 = new Question(question);
        question1 = questionRepo.save(question1);
        List<Question> list = new ArrayList<>();
        list.add(question1);

        test.setTitle(title);
        test.setQuestions(list);
        test.setModule(module);
        test.setSections(0);
        test.setGradingSystem(null);

        testRepo.save(test);

        module.getTests().add(test);
        moduleRepo.save(module);
    }

    // Выставление оценки за тест учителем
    public void putMark(Student student, Test test, int mark) {
        test.getStudentsMarks().put(student.getId(), mark);
        student.getAllMarks().put(test.getId(), mark);

        testRepo.save(test);
        studentRepo.save(student);
    }

    // Перемешка правильных ответов
    private void shuffle(List<Question> questions) {
        for (Question question : questions) {
            List<String> answerOptions = question.getAnswersOptions();
            List<String> newAnswerOptions = new ArrayList<>();
            List<Boolean> correctAnswer = question.getCorrectAnswer();
            List<Boolean> newCorrectAnswer = new ArrayList<>();
            int size = answerOptions.size() - 1;
            int step = (int) (Math.random() * ++size);
            int j = 0;

            while (newAnswerOptions.size() != answerOptions.size()) {
                int replace = (j + step) % size;
                if (newAnswerOptions.contains(answerOptions.get(replace))) {
                    continue;
                }
                newAnswerOptions.add(answerOptions.get(replace));
                newCorrectAnswer.add(correctAnswer.get(replace));
                j++;
            }

            question.setAnswersOptions(newAnswerOptions);
            question.setCorrectAnswer(newCorrectAnswer);
            questionRepo.save(question);
        }
    }

    // Обработка правильных ответов, пришедших с клиента
    private List<Boolean> parseHtmlCheckbox(List<String> htmlCorrectAnswers) {
        List<Boolean> correctAnswers = new ArrayList<>();

        for (int i = 0 ; i < htmlCorrectAnswers.size(); i++) {
            String answer = htmlCorrectAnswers.get(i);
            if (answer.equals("on")) {
                correctAnswers.add(true);
                htmlCorrectAnswers.remove(i + 1);
            } else {
                correctAnswers.add(false);
            }
        }

        return correctAnswers;
    }

    // Обработка вопросов, пришедших с клиента
    private List<Question> parseHtmlQuestions(List<String> htmlQuestions, List<Boolean> correctAnswers,
                                              List<String> htmlAnswersOptions) {
        List<Question> questions = new ArrayList<>();

        int k = 0;
        for (String htmlQuestion : htmlQuestions) {
            Question question = new Question();
            question.setQuestion(htmlQuestion);
            List<String> answerOptions = new ArrayList<>();
            List<Boolean> correctAnswer = new ArrayList<>();

            for (int j = 1; j <= 6; j++) {
                String answer = htmlAnswersOptions.get(k);
                boolean isCorrect = correctAnswers.get(k);

                if (!StringUtils.isEmpty(answer) && !answerOptions.contains(answer)) {
                    answerOptions.add(answer);
                    correctAnswer.add(isCorrect);
                }
                k++;
            }

            question.setAnswersOptions(answerOptions);
            question.setCorrectAnswer(correctAnswer);
            questionRepo.save(question);
            questions.add(question);
        }

        return questions;
    }

}

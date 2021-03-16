package testing_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import testing_system.domain.group.EducationGroup;
import testing_system.domain.group.Module;
import testing_system.domain.people.Roles;
import testing_system.domain.people.User;
import testing_system.domain.test.Question;
import testing_system.domain.test.Test;
import testing_system.repos.group.EducationGroupRepo;
import testing_system.repos.group.ModuleRepo;
import testing_system.repos.people.StudentRepo;
import testing_system.repos.people.TeacherRepo;
import testing_system.repos.test.QuestionRepo;
import testing_system.repos.test.TestRepo;

import java.util.*;

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

    // Создание ногово теста
    public void addTest(String title, List<String> htmlQuestions, List<String> htmlAnswersOptions,
                        List<String> htmlCorrectAnswers, Module module, List<Integer> marks, int section) {
        List<Boolean> correctAnswers = parseHtmlCheckbox(htmlCorrectAnswers);
        List<Question> questions = parseHtmlQuestions(htmlQuestions, correctAnswers, htmlAnswersOptions);

        Map<Integer, Float> gradingSystem = new HashMap<>(marks.size());

        int index = 0;
        for (int i = 5; i >= 3; i--) {
            gradingSystem.put(i, (float) ((float) marks.get(index) / 100.0));
        }

        Test test = new Test();
        test.setTitle(title);
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
            strings[2] = sAverage[0] + "." + sAverage[2].charAt(0) + sAverage[2].charAt(1);
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

                if (!StringUtils.isEmpty(answer)) {
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
        questions.removeIf(x -> contains2(questions, x.getId()));
        return questions;
    }

    private boolean contains2(List<Question> questions, long id) {
        int count = 0;
        for (Question question : questions) {
            if (count >= 2) {
                return true;
            }
            if (question.getId() == id) {
                count++;
            }
        }
        return count >= 2;
    }

}

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

    public void addTest(String title, List<String> htmlQuestions, List<String> htmlAnswersOptions,
                        List<String> htmlCorrectAnswers, Module module, List<Integer> marks) {
        List<Boolean> correctAnswers = new ArrayList<>();
        List<Question> questions = new ArrayList<>();

        for (int i = 0 ; i < htmlCorrectAnswers.size(); i++) {
            String answer = htmlCorrectAnswers.get(i);
            if (answer.equals("on")) {
                correctAnswers.add(true);
                htmlCorrectAnswers.remove(i + 1);
            } else {
                correctAnswers.add(false);
            }
        }

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

        Map<Integer, Integer> gradingSystem = new HashMap<>(marks.size());

        int index = 0;
        for (int i = 5; i >= 3; i--) {
            gradingSystem.put(i, marks.get(index));
        }

        Test test = new Test();
        test.setTitle(title);
        test.setQuestions(questions);
        test.setGradingSystem(gradingSystem);
        test.setModule(module);
        testRepo.save(test);

        module.getTests().add(test);
        moduleRepo.save(module);

    }

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

    public boolean check(User user, EducationGroup educationGroup) {

        for (User u : educationGroup.getStudents()) {
            if (u.getId() == user.getId()) {
                return true;
            }
        }
        for (User u : educationGroup.getTeachers()) {
            if (u.getId() == user.getId()) {
                return true;
            }
        }

        return user.getRoles().contains(Roles.TEACHER_ADMIN);
    }

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

        Module module = new Module();
        module.setTitle(title);
        module.setEducationGroup(educationGroup);
        moduleRepo.save(module);

        educationGroup.getModules().add(module);
        educationGroupRepo.save(educationGroup);

        return true;
    }

}
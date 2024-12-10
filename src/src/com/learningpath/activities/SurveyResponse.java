package src.com.learningpath.activities;

import src.com.learningpath.users.Student;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SurveyResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Student student;
    private Survey survey;
    private Map<String, String> answers;

    public SurveyResponse(Student student, Survey survey) {
        this.student = student;
        this.survey = survey;
        this.answers = new HashMap<>();
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    public Map<String, String> getAnswers() {
        return answers;
    }

    public void addAnswer(String question, String answer) {
        answers.put(question, answer);
    }
}



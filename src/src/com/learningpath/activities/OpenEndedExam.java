package src.com.learningpath.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class OpenEndedExam extends Activity implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<OpenEndedQuestion> questions;
    private List<OpenEndedQuestion> examQuestions;
    private List<OpenEndedResponse> examResponses;
    private String instructionsFile;

    public OpenEndedExam(String title, String description, String objective, int difficultyLevel,
                         int expectedDuration, boolean isMandatory, Set<ActivityType> types,
                         List<OpenEndedQuestion> openEndedQuestions) {
        super(title, description, objective, difficultyLevel, expectedDuration, isMandatory);
        this.examQuestions = openEndedQuestions;
        this.examResponses = new ArrayList<>();
        this.questions = new ArrayList<>();
        // types no se usa directamente acá, pero se podría almacenar si se desea
    }

    public List<OpenEndedQuestion> getExamQuestions() {
        return examQuestions;
    }

    public void addExamQuestion(OpenEndedQuestion question) {
        examQuestions.add(question);
    }

    public List<OpenEndedResponse> getExamResponses() {
        return examResponses;
    }

    public void addExamResponse(OpenEndedResponse response) {
        examResponses.add(response);
    }

    public void addQuestion(OpenEndedQuestion question) {
        if (question != null) {
            questions.add(question);
        }
    }

    public List<OpenEndedQuestion> getQuestions() {
        return questions;
    }

    @Override
    public String getType() {
        return "OpenEndedExam";
    }

    public String getInstructionsFile() {
        return instructionsFile;
    }

    public void setInstructionsFile(String instructionsFile) {
        this.instructionsFile = instructionsFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OpenEndedExam)) return false;
        OpenEndedExam that = (OpenEndedExam) o;
        return getTitle().equals(that.getTitle()) &&
               getDescription().equals(that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getDescription());
    }
}


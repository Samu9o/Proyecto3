package src.com.learningpath.activities;

import java.util.List;
import java.util.Objects;

public class Quiz extends Activity {
    private List<Question> questions;
    private double passingScore;

    public Quiz(String title, String description, String objective, int difficultyLevel, int expectedDuration, boolean isMandatory, List<Question> questions, double passingScore) {
        super(title, description, objective, difficultyLevel, expectedDuration, isMandatory);
        this.questions = questions;
        this.passingScore = passingScore;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public double getPassingScore() {
        return passingScore;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Survey)) return false;
        Survey other = (Survey) o;
        return this.getTitle().equals(other.getTitle()) &&
               this.getDescription().equals(other.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getDescription());
    }


    @Override
    public String getType() {
        return "Quiz";
    }
    
}
	
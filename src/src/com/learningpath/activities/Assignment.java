package src.com.learningpath.activities;

import java.io.Serializable;

public class Assignment extends Activity implements Serializable {
    private String submissionInstructions;

    public Assignment(String title, String description, String objective, int difficultyLevel, int expectedDuration, boolean isMandatory, String submissionInstructions) {
        super(title, description, objective, difficultyLevel, expectedDuration, isMandatory);
        this.submissionInstructions = submissionInstructions;
    }

    public String getSubmissionInstructions() {
        return submissionInstructions;
    }

    @Override
    public String getType() {
        return "Assignment";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Assignment)) return false;
        Assignment that = (Assignment) o;
        return this.getTitle().equals(that.getTitle()) &&
               this.getDescription().equals(that.getDescription());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(getTitle(), getDescription());
    }
}

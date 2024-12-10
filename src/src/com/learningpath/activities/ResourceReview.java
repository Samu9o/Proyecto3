package src.com.learningpath.activities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clase que representa una actividad de revisión de un recurso.
 * Incluye un enlace al recurso y un conjunto de instrucciones para el estudiante.
 */
public class ResourceReview extends Activity implements Serializable {
    private String resourceLink;
    private String instructions;

    public ResourceReview(String title, String description, String objective, int difficultyLevel, int expectedDuration, boolean isMandatory, String resourceLink, String instructions) {
        super(title, description, objective, difficultyLevel, expectedDuration, isMandatory);
        this.resourceLink = resourceLink;
        this.instructions = instructions;
    }

    public ResourceReview(String title, String description, String objective, int difficultyLevel, int expectedDuration, boolean isMandatory, String resourceLink) {
        super(title, description, objective, difficultyLevel, expectedDuration, isMandatory);
        this.resourceLink = resourceLink;
        this.instructions = "";
    }

    public String getResourceLink() {
        return resourceLink;
    }

    public String getInstructions() {
        return instructions;
    }

    @Override
    public String getType() {
        return "Resource Review";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceReview)) return false;
        ResourceReview that = (ResourceReview) o;
        return getTitle().equals(that.getTitle()) &&
               getDescription().equals(that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getDescription());
    }
}


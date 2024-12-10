package src.com.learningpath.activities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Clase abstracta que representa una Actividad en un Learning Path.
 */
public abstract class Activity implements Serializable {
    private static final long serialVersionUID = 1L;

    // Campos comunes a todas las actividades
    protected String title;
    private String instructions;
    protected String description;
    protected String objective;
    protected int difficultyLevel;
    protected int expectedDuration; // en minutos
    protected List<Activity> suggestedPrerequisites;
    protected Date deadline;
    protected boolean isMandatory;

    /**
     * Constructor para Activity.
     *
     * @param title            Título de la actividad.
     * @param description      Descripción de la actividad.
     * @param objective        Objetivo de la actividad.
     * @param difficultyLevel  Nivel de dificultad de la actividad.
     * @param expectedDuration Duración esperada en minutos.
     * @param isMandatory      Si la actividad es obligatoria.
     */
    public Activity(String title, String description, String objective, int difficultyLevel, int expectedDuration, boolean isMandatory) {
        this.title = title;
        this.description = description;
        this.objective = objective;
        this.difficultyLevel = difficultyLevel;
        this.expectedDuration = expectedDuration;
        this.isMandatory = isMandatory;
    }

    /**
     * Constructor alternativo para Activity con tipos de actividad.
     *
     * @param title            Título de la actividad.
     * @param description      Descripción de la actividad.
     * @param objective        Objetivo de la actividad.
     * @param difficultyLevel  Nivel de dificultad de la actividad.
     * @param expectedDuration Duración esperada en minutos.
     * @param isMandatory      Si la actividad es obligatoria.
     * @param types            Tipos de actividad.
     */
    public Activity(String title, String description, String objective, int difficultyLevel, int expectedDuration,
                   boolean isMandatory, Set<ActivityType> types) {
        this(title, description, objective, difficultyLevel, expectedDuration, isMandatory);
        // Inicializar tipos si es necesario
    }

    // Getters para los campos protegidos
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getObjective() {
        return objective;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }
    public String getInstructions() {
        return instructions;
    }
   
    public int getExpectedDuration() {
        return expectedDuration;
    }

    public List<Activity> getSuggestedPrerequisites() {
        return suggestedPrerequisites;
    }

    public Date getDeadline() {
        return deadline;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    // Método abstracto para obtener el tipo de actividad
    public abstract String getType();

    // Sobrescribir equals y hashCode basados en title y description (asumiendo que juntos son únicos)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Activity)) return false;
        Activity other = (Activity) obj;
        return title.equals(other.title) && description.equals(other.description);
    }

    @Override
    public int hashCode() {
        return title.hashCode() * 31 + description.hashCode();
    }
}


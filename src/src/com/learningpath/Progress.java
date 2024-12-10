package src.com.learningpath;

import src.com.learningpath.activities.*;
import src.com.learningpath.users.Student;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase que representa el progreso de un estudiante en un Learning Path.
 */
public class Progress implements Serializable {
    private static final long serialVersionUID = 2L; // Actualizado después de añadir nuevos campos

    private Student student;
    private LearningPath learningPath;
    private Map<Activity, ActivityStatus> activityStatuses;
    private Map<Survey, SurveyResponse> surveyResponses;
    private Map<OpenEndedExam, OpenEndedResponse> examResponses;
    private Map<Quiz, List<Integer>> quizResponses;

    // Nuevo mapa para almacenar la fecha de completado/entrega de las actividades.
    private Map<Activity, LocalDate> completionDates;

    /**
     * Constructor para crear un progreso de Learning Path.
     *
     * @param student      El estudiante.
     * @param learningPath El Learning Path.
     */
    public Progress(Student student, LearningPath learningPath) {
        this.student = student;
        this.learningPath = learningPath;
        this.activityStatuses = new HashMap<>();
        this.surveyResponses = new HashMap<>();
        this.examResponses = new HashMap<>();
        this.quizResponses = new HashMap<>();
        this.completionDates = new HashMap<>();

        // Inicializar estados de actividades
        for (Activity activity : learningPath.getActivities()) {
            activityStatuses.put(activity, ActivityStatus.PENDING);
        }
    }

    // Getters y setters

    /**
     * Obtiene el estudiante asociado a este progreso.
     */
    public Student getStudent() {
        return student;
    }

    /**
     * Establece el estudiante asociado a este progreso.
     */
    public void setStudent(Student student) {
        this.student = student;
    }

    /**
     * Obtiene el Learning Path asociado a este progreso.
     */
    public LearningPath getLearningPath() {
        return learningPath;
    }

    /**
     * Establece el Learning Path asociado a este progreso.
     */
    public void setLearningPath(LearningPath learningPath) {
        this.learningPath = learningPath;
    }

    /**
     * Obtiene el estado de todas las actividades.
     */
    public Map<Activity, ActivityStatus> getActivityStatuses() {
        return activityStatuses;
    }

    /**
     * Obtiene las respuestas a las encuestas.
     */
    public Map<Survey, SurveyResponse> getSurveyResponses() {
        return surveyResponses;
    }

    /**
     * Obtiene las respuestas a los exámenes de preguntas abiertas.
     */
    public Map<OpenEndedExam, OpenEndedResponse> getExamResponses() {
        return examResponses;
    }

    /**
     * Obtiene las respuestas a los quizzes.
     */
    public Map<Quiz, List<Integer>> getQuizResponses() {
        return quizResponses;
    }

    /**
     * Obtiene las fechas de completado de las actividades.
     */
    public Map<Activity, LocalDate> getCompletionDates() {
        return completionDates;
    }

    /**
     * Establece las fechas de completado de las actividades.
     */
    public void setCompletionDates(Map<Activity, LocalDate> completionDates) {
        this.completionDates = completionDates;
    }

    /**
     * Actualiza el estado de una actividad.
     *
     * @param activity La actividad a actualizar.
     * @param status   El nuevo estado.
     */
    public void updateActivityStatus(Activity activity, ActivityStatus status) {
        activityStatuses.put(activity, status);
        // Si la actividad se completa o se entrega (COMPLETED o SUBMITTED),
        // registramos la fecha actual como fecha de finalización.
        if (status == ActivityStatus.COMPLETED || status == ActivityStatus.SUBMITTED) {
            completionDates.put(activity, LocalDate.now());
        }
    }

    /**
     * Obtiene el estado de una actividad.
     *
     * @param activity La actividad.
     * @return El estado de la actividad.
     */
    public ActivityStatus getActivityStatus(Activity activity) {
        return activityStatuses.get(activity);
    }

    /**
     * Calcula el porcentaje de actividades completadas.
     *
     * @return El porcentaje completado.
     */
    public double calculateCompletionPercentage() {
        int total = activityStatuses.size();
        long completed = activityStatuses.values().stream()
                .filter(status -> status == ActivityStatus.COMPLETED || status == ActivityStatus.SUBMITTED)
                .count();
        return (double) completed / total * 100;
    }

    /**
     * Añade una respuesta a una encuesta específica.
     */
    public void addSurveyResponse(Survey survey, SurveyResponse response) {
        surveyResponses.put(survey, response);
    }

    /**
     * Obtiene la respuesta de una encuesta específica.
     */
    public SurveyResponse getSurveyResponse(Survey survey) {
        return surveyResponses.get(survey);
    }

    /**
     * Añade una respuesta a un examen de preguntas abiertas específico.
     */
    public void addExamResponse(OpenEndedExam exam, OpenEndedResponse response) {
        examResponses.put(exam, response);
    }

    /**
     * Obtiene la respuesta de un examen de preguntas abiertas específico.
     */
    public OpenEndedResponse getExamResponse(OpenEndedExam exam) {
        return examResponses.get(exam);
    }

    /**
     * Guarda las respuestas seleccionadas por el estudiante en un Quiz.
     */
    public void saveQuizResponses(Quiz quiz, List<Integer> chosenOptions) {
        quizResponses.put(quiz, chosenOptions);
    }

    /**
     * Obtiene las respuestas seleccionadas por el estudiante en un Quiz.
     */
    public List<Integer> getQuizResponses(Quiz quiz) {
        return quizResponses.get(quiz);
    }

    /**
     * Obtiene la fecha en que una actividad fue completada o entregada.
     * Devuelve null si la actividad no se ha completado.
     *
     * @param a La actividad.
     * @return La fecha de completado o null si no existe.
     */
    public LocalDate getCompletionDate(Activity a) {
        return completionDates.get(a);
    }

    /**
     * Método readObject para asegurar que completionDates está inicializado después de la deserialización.
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        if (completionDates == null) {
            completionDates = new HashMap<>();
        }
        if (activityStatuses == null) {
            activityStatuses = new HashMap<>();
            for (Activity activity : learningPath.getActivities()) {
                activityStatuses.put(activity, ActivityStatus.PENDING);
            }
        }
    }
}




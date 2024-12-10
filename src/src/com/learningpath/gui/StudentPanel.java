package src.com.learningpath.gui;

import src.com.learningpath.users.Student;
import src.com.learningpath.users.User;
import src.com.learningpath.*;
import src.com.learningpath.activities.*;
import src.com.learningpath.activities.OpenEndedResponse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Panel para los estudiantes donde pueden gestionar sus Learning Paths.
 * Permite:
 * - Inscribirse en LPs disponibles.
 * - Ver sus LPs inscritos y su progreso.
 * - Interactuar con las actividades (Quiz, Survey, OpenEndedExam, ResourceReview).
 */
public class StudentPanel extends JPanel {
    private MainFrame mainFrame;
    private User currentUser;

    private JLabel welcomeLabel;
    private JButton logoutButton;
    private JTabbedPane tabbedPane;

    // Panel para Learning Paths Disponibles
    private JTable availableLPTable;
    private DefaultTableModel availableLPTableModel;
    private JButton enrollButton;

    // Panel para Mis Learning Paths
    private JTable myLPTable;
    private DefaultTableModel myLPTableModel;
    private JButton viewDetailsButton;

    /**
     * Constructor de StudentPanel.
     *
     * @param mainFrame La instancia principal de MainFrame.
     */
    public StudentPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        // Panel superior con el label de bienvenida y botón de logout
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        welcomeLabel = new JLabel("Bienvenido, Estudiante");
        topPanel.add(welcomeLabel);

        logoutButton = new JButton("Cerrar Sesión");
        logoutButton.addActionListener(e -> mainFrame.logout());
        topPanel.add(logoutButton);

        add(topPanel, BorderLayout.NORTH);

        // Pestañas principales
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Learning Paths Disponibles", createAvailableLPPanel());
        tabbedPane.addTab("Mis Learning Paths", createMyLPPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Actualiza los datos del estudiante en el panel.
     *
     * @param user El usuario actual (Estudiante).
     */
    public void updateData(User user) {
        this.currentUser = user;
        if (user instanceof Student) {
            welcomeLabel.setText("Bienvenido, " + user.getName() + " (Estudiante)");
            loadAvailableLearningPaths();
            loadMyLearningPaths();
        }
    }

    /**
     * Crea el panel para "Learning Paths Disponibles".
     *
     * @return El panel creado.
     */
    private JPanel createAvailableLPPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        availableLPTableModel = new DefaultTableModel(new String[]{"Título", "Descripción", "Dificultad", "Duración (min)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        availableLPTable = new JTable(availableLPTableModel);
        availableLPTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(availableLPTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        enrollButton = new JButton("Inscribirse");
        enrollButton.addActionListener(e -> enrollInLearningPath());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(enrollButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Crea el panel para "Mis Learning Paths".
     *
     * @return El panel creado.
     */
    private JPanel createMyLPPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        myLPTableModel = new DefaultTableModel(new String[]{"Título", "Descripción", "Dificultad", "Duración (min)", "Progreso (%)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        myLPTable = new JTable(myLPTableModel);
        myLPTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(myLPTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        viewDetailsButton = new JButton("Ver Detalles");
        viewDetailsButton.addActionListener(e -> viewLearningPathDetails());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(viewDetailsButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Carga los Learning Paths disponibles en la tabla.
     */
    private void loadAvailableLearningPaths() {
        availableLPTableModel.setRowCount(0);
        Student student = (Student) currentUser;
        List<LearningPath> enrolledLPs = new ArrayList<>();

        // Obtener los Learning Paths en los que el estudiante ya está inscrito
        for (Progress p : mainFrame.getProgresses()) {
            if (p.getStudent().equals(student)) {
                enrolledLPs.add(p.getLearningPath());
            }
        }

        // Listar los Learning Paths no inscritos
        for (LearningPath lp : mainFrame.getLearningPaths()) {
            if (!enrolledLPs.contains(lp)) {
                availableLPTableModel.addRow(new Object[]{
                        lp.getTitle(),
                        lp.getDescription(),
                        lp.getDifficultyLevel(),
                        lp.getDuration()
                });
            }
        }
    }

    /**
     * Permite al estudiante inscribirse en un Learning Path seleccionado.
     */
    private void enrollInLearningPath() {
        int selectedRow = availableLPTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona un Learning Path para inscribirte.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String title = (String) availableLPTableModel.getValueAt(selectedRow, 0);
        LearningPath selectedLP = getLearningPathByTitle(title);

        if (selectedLP != null) {
            Student student = (Student) currentUser;
            Progress existingProgress = mainFrame.findProgress(student, selectedLP);
            if (existingProgress != null) {
                JOptionPane.showMessageDialog(this, "Ya estás inscrito en este Learning Path.", "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Progress newProgress = new Progress(student, selectedLP);
            try {
                mainFrame.addProgress(newProgress);
                JOptionPane.showMessageDialog(this, "Inscripción exitosa en " + selectedLP.getTitle(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
                loadAvailableLearningPaths();
                loadMyLearningPaths();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al inscribirte: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Carga los Learning Paths inscritos por el estudiante en la tabla.
     */
    private void loadMyLearningPaths() {
        myLPTableModel.setRowCount(0);
        Student student = (Student) currentUser;

        for (Progress p : mainFrame.getProgresses()) {
            if (p.getStudent().equals(student)) {
                LearningPath lp = p.getLearningPath();
                double progress = p.calculateCompletionPercentage();
                myLPTableModel.addRow(new Object[]{
                        lp.getTitle(),
                        lp.getDescription(),
                        lp.getDifficultyLevel(),
                        lp.getDuration(),
                        String.format("%.2f", progress)
                });
            }
        }
    }

    /**
     * Muestra los detalles del Learning Path seleccionado, incluyendo sus actividades.
     */
    private void viewLearningPathDetails() {
        int selectedRow = myLPTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona un Learning Path para ver los detalles.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String title = (String) myLPTableModel.getValueAt(selectedRow, 0);
        final LearningPath selectedLP = getLearningPathByTitle(title);

        if (selectedLP != null) {
            Progress progress = mainFrame.findProgress((Student) currentUser, selectedLP);
            if (progress == null) {
                JOptionPane.showMessageDialog(this, "No se encontró el progreso para este Learning Path.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFrame detailsFrame = new JFrame("Detalles de Learning Path - " + selectedLP.getTitle());
            detailsFrame.setSize(800, 600);
            detailsFrame.setLocationRelativeTo(this);
            detailsFrame.setLayout(new BorderLayout());

            JTextArea infoArea = new JTextArea();
            infoArea.setEditable(false);
            infoArea.setText("Título: " + selectedLP.getTitle() + "\n" +
                    "Descripción: " + selectedLP.getDescription() + "\n" +
                    "Objetivos: " + selectedLP.getObjectives() + "\n" +
                    "Nivel de Dificultad: " + selectedLP.getDifficultyLevel() + "\n" +
                    "Duración: " + selectedLP.getDuration() + " minutos\n" +
                    "Progreso: " + String.format("%.2f", progress.calculateCompletionPercentage()) + "%");
            infoArea.setFont(new Font("Arial", Font.PLAIN, 14));
            JScrollPane infoScroll = new JScrollPane(infoArea);
            detailsFrame.add(infoScroll, BorderLayout.NORTH);

            DefaultTableModel activitiesTableModel = new DefaultTableModel(new String[]{"Tipo", "Título", "Descripción", "Estado"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable activitiesTable = new JTable(activitiesTableModel);
            activitiesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane activitiesScroll = new JScrollPane(activitiesTable);
            detailsFrame.add(activitiesScroll, BorderLayout.CENTER);

            JButton interactButton = new JButton("Interactuar con Actividad");
            interactButton.addActionListener(e -> {
                int row = activitiesTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(detailsFrame, "Por favor, selecciona una actividad para interactuar.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                interactWithActivity(activitiesTable, activitiesTableModel, progress, selectedLP);
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(interactButton);
            detailsFrame.add(buttonPanel, BorderLayout.SOUTH);

            List<Activity> activities = selectedLP.getActivities();
            for (Activity activity : activities) {
                ActivityStatus status = progress.getActivityStatus(activity);
                activitiesTableModel.addRow(new Object[]{
                        activity.getType(),
                        activity.getTitle(),
                        activity.getDescription(),
                        status
                });
            }

            detailsFrame.setVisible(true);
        }
    }

    private LearningPath getLearningPathByTitle(String title) {
        for (LearningPath lp : mainFrame.getLearningPaths()) {
            if (lp.getTitle().equals(title)) {
                return lp;
            }
        }
        return null;
    }

    /**
     * Permite al estudiante interactuar con la actividad seleccionada.
     */
    private void interactWithActivity(JTable activitiesTable, DefaultTableModel activitiesTableModel, Progress progress, LearningPath selectedLP) {
        int selectedRow = activitiesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona una actividad para interactuar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String activityType = (String) activitiesTableModel.getValueAt(selectedRow, 0);
        String activityTitle = (String) activitiesTableModel.getValueAt(selectedRow, 1);
        Activity selectedActivity = getActivityByTitleAndType(selectedLP, activityTitle, activityType);

        if (selectedActivity == null) {
            JOptionPane.showMessageDialog(this, "No se encontró la actividad seleccionada.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ActivityStatus status = progress.getActivityStatus(selectedActivity);
        if (status == ActivityStatus.COMPLETED || status == ActivityStatus.SUBMITTED) {
            JOptionPane.showMessageDialog(this, "Ya has completado esta actividad.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        switch (activityType) {
            case "Quiz":
                interactWithQuiz((Quiz) selectedActivity, progress, selectedLP);
                break;
            case "Survey":
                interactWithSurvey((Survey) selectedActivity, progress, selectedLP);
                break;
            case "OpenEndedExam":
                interactWithOpenEndedExam((OpenEndedExam) selectedActivity, progress, selectedLP);
                break;
            case "Resource Review":
                interactWithResourceReview((ResourceReview) selectedActivity, progress, selectedLP);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Tipo de actividad no soportado.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        activitiesTableModel.setValueAt(progress.getActivityStatus(selectedActivity), selectedRow, 3);
    }

    private Activity getActivityByTitleAndType(LearningPath lp, String title, String activityType) {
        for (Activity activity : lp.getActivities()) {
            if (activity.getTitle().equals(title) && activity.getType().equals(activityType)) {
                return activity;
            }
        }
        return null;
    }

    /**
     * Interacción con un Quiz.
     */
    private void interactWithQuiz(Quiz quiz, Progress progress, LearningPath lp) {
        List<Question> questions = quiz.getQuestions();
        JPanel quizPanel = new JPanel(new GridLayout(questions.size(), 1, 10, 10));
        JRadioButton[][] optionsButtons = new JRadioButton[questions.size()][4];
        ButtonGroup[] groups = new ButtonGroup[questions.size()];

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            JPanel questionPanel = new JPanel(new BorderLayout());
            questionPanel.setBorder(BorderFactory.createTitledBorder("Pregunta " + (i + 1)));
            JLabel qLabel = new JLabel("<html>" + q.getQuestionText() + "</html>");
            questionPanel.add(qLabel, BorderLayout.NORTH);

            JPanel optionsPanel = new JPanel(new GridLayout(4, 1));
            groups[i] = new ButtonGroup();
            for (int j = 0; j < 4; j++) {
                optionsButtons[i][j] = new JRadioButton(q.getOptions()[j]);
                groups[i].add(optionsButtons[i][j]);
                optionsPanel.add(optionsButtons[i][j]);
            }
            questionPanel.add(optionsPanel, BorderLayout.CENTER);
            quizPanel.add(questionPanel);
        }

        int result = JOptionPane.showConfirmDialog(this, quizPanel, "Quiz: " + quiz.getTitle(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            List<Integer> selectedOptions = new ArrayList<>();
            for (int i = 0; i < questions.size(); i++) {
                boolean answered = false;
                for (int j = 0; j < 4; j++) {
                    if (optionsButtons[i][j].isSelected()) {
                        selectedOptions.add(j);
                        answered = true;
                        break;
                    }
                }
                if (!answered) {
                    JOptionPane.showMessageDialog(this, "Por favor, responde todas las preguntas.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            progress.saveQuizResponses(quiz, selectedOptions);

            // Evaluar respuestas
            int correct = 0;
            for (int i = 0; i < questions.size(); i++) {
                if (selectedOptions.get(i) == questions.get(i).getCorrectOptionIndex()) {
                    correct++;
                }
            }

            double score = ((double) correct / questions.size()) * 100.0;
            boolean passed = score >= quiz.getPassingScore();

            String message = "Has obtenido " + correct + " de " + questions.size() + " respuestas correctas.\n" +
                    "Puntuación: " + String.format("%.2f", score) + "%.\n" +
                    (passed ? "Has aprobado el Quiz." : "No has alcanzado la puntuación mínima para aprobar.");

            JOptionPane.showMessageDialog(this, message, "Resultado del Quiz", JOptionPane.INFORMATION_MESSAGE);

            if (passed) {
                progress.updateActivityStatus(quiz, ActivityStatus.COMPLETED);
            } else {
                progress.updateActivityStatus(quiz, ActivityStatus.FAILED);
            }

            mainFrame.saveAllData();
            mainFrame.reloadAllData();
            this.updateData(mainFrame.getCurrentUser());// Vuelves a cargar datos desde DataManager
           
			 // Actualizas la vista del profesor
            
        }
    }

    /**
     * Interacción con una Survey.
     */
    private void interactWithSurvey(Survey survey, Progress progress, LearningPath lp) {
        List<SurveyQuestion> questions = survey.getSurveyQuestions();
        JPanel surveyPanel = new JPanel(new GridLayout(questions.size(), 1, 10, 10));

        for (int i = 0; i < questions.size(); i++) {
            SurveyQuestion q = questions.get(i);
            JPanel questionPanel = new JPanel(new BorderLayout());
            questionPanel.setBorder(BorderFactory.createTitledBorder("Pregunta " + (i + 1)));
            JLabel qLabel = new JLabel("<html>" + q.getQuestionText() + "</html>");
            JTextArea answerArea = new JTextArea(3, 50);
            answerArea.setLineWrap(true);
            answerArea.setWrapStyleWord(true);
            JScrollPane answerScroll = new JScrollPane(answerArea);
            questionPanel.add(qLabel, BorderLayout.NORTH);
            questionPanel.add(answerScroll, BorderLayout.CENTER);
            surveyPanel.add(questionPanel);
        }

        int result = JOptionPane.showConfirmDialog(this, surveyPanel, "Survey: " + survey.getTitle(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            List<String> responses = new ArrayList<>();
            Component[] components = surveyPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel qPanel = (JPanel) comp;
                    JScrollPane scroll = (JScrollPane) qPanel.getComponent(1);
                    JTextArea answerArea = (JTextArea) scroll.getViewport().getView();
                    String answer = answerArea.getText().trim();
                    if (answer.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Por favor, responde todas las preguntas.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    responses.add(answer);
                }
            }

            SurveyResponse surveyResponse = new SurveyResponse((Student) currentUser, survey);
            for (int i = 0; i < responses.size(); i++) {
                String question = questions.get(i).getQuestionText();
                String answer = responses.get(i);
                surveyResponse.addAnswer(question, answer);
            }
            progress.addSurveyResponse(survey, surveyResponse);
            progress.updateActivityStatus(survey, ActivityStatus.SUBMITTED);

            JOptionPane.showMessageDialog(this, "Gracias por completar la Survey.", "Survey Completada", JOptionPane.INFORMATION_MESSAGE);

            mainFrame.saveAllData();
        }
    }

    /**
     * Interacción con un Examen de Preguntas Abiertas.
     */
    private void interactWithOpenEndedExam(OpenEndedExam exam, Progress progress, LearningPath lp) {
        // Mostrar instrucciones si existen
        if (exam.getInstructionsFile() != null) {
            try {
                String instructionsText = new String(Files.readAllBytes(Paths.get(exam.getInstructionsFile())), StandardCharsets.UTF_8);
                JTextArea instructionsArea = new JTextArea(instructionsText, 10, 50);
                instructionsArea.setEditable(false);
                instructionsArea.setLineWrap(true);
                instructionsArea.setWrapStyleWord(true);
                JScrollPane scrollPane = new JScrollPane(instructionsArea);
                scrollPane.setPreferredSize(new Dimension(600, 200));
                JOptionPane.showMessageDialog(this, scrollPane, "Instrucciones del Examen", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "No se pudieron leer las instrucciones: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        List<OpenEndedQuestion> questions = exam.getQuestions();
        JPanel examPanel = new JPanel(new GridLayout(questions.size(), 1, 10, 10));

        for (int i = 0; i < questions.size(); i++) {
            OpenEndedQuestion q = questions.get(i);
            JPanel questionPanel = new JPanel(new BorderLayout());
            questionPanel.setBorder(BorderFactory.createTitledBorder("Pregunta " + (i + 1)));
            JLabel qLabel = new JLabel("<html>" + q.getQuestionText() + "</html>");
            JTextArea answerArea = new JTextArea(3, 50);
            answerArea.setLineWrap(true);
            answerArea.setWrapStyleWord(true);
            JScrollPane answerScroll = new JScrollPane(answerArea);
            questionPanel.add(qLabel, BorderLayout.NORTH);
            questionPanel.add(answerScroll, BorderLayout.CENTER);
            examPanel.add(questionPanel);
        }

        int result = JOptionPane.showConfirmDialog(this, examPanel, "Examen: " + exam.getTitle(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            List<String> responses = new ArrayList<>();
            Component[] components = examPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel qPanel = (JPanel) comp;
                    JScrollPane scroll = (JScrollPane) qPanel.getComponent(1);
                    JTextArea answerArea = (JTextArea) scroll.getViewport().getView();
                    String answer = answerArea.getText().trim();
                    if (answer.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Por favor, responde todas las preguntas.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    responses.add(answer);
                }
            }

            OpenEndedResponse examResponse = new OpenEndedResponse((Student) currentUser, exam);
            for (int i = 0; i < responses.size(); i++) {
                String question = questions.get(i).getQuestionText();
                String answer = responses.get(i);
                examResponse.addAnswer(question, answer);
            }
            progress.addExamResponse(exam, examResponse);
            progress.updateActivityStatus(exam, ActivityStatus.SUBMITTED);

            JOptionPane.showMessageDialog(this, "Gracias por completar el Examen.", "Examen Completado", JOptionPane.INFORMATION_MESSAGE);

            mainFrame.saveAllData();
        }
    }

    /**
     * Interacción con una Revisión de Recurso.
     */
    private void interactWithResourceReview(ResourceReview resourceReview, Progress progress, LearningPath lp) {
        JTextArea instructionsArea = new JTextArea(resourceReview.getInstructions());
        instructionsArea.setEditable(false);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        instructionsArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(instructionsArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Revisión de Recurso: " + resourceReview.getTitle(), JOptionPane.INFORMATION_MESSAGE);

        progress.updateActivityStatus(resourceReview, ActivityStatus.COMPLETED);

        JOptionPane.showMessageDialog(this, "Has completado la Revisión de Recurso.", "Revisión Completada", JOptionPane.INFORMATION_MESSAGE);

        mainFrame.saveAllData();
    }
}


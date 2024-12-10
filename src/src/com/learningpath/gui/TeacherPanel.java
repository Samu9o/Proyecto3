package src.com.learningpath.gui;

import src.com.learningpath.users.User;
import src.com.learningpath.users.Student;
import src.com.learningpath.users.Teacher;
import src.com.learningpath.users.Role;
import src.com.learningpath.*;
import src.com.learningpath.activities.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class TeacherPanel extends JPanel {
    private MainFrame mainFrame;
    private User currentUser;

    private JLabel welcomeLabel;
    private JButton logoutButton;
    private JTabbedPane tabbedPane;

    private JTable myLPTable;
    private DefaultTableModel myLPTableModel;

    private int currentYear = LocalDate.now().getYear();
    private YearActivityPanel yearActivityPanel;
    private JPanel yearActivityPanelContainer;

    public TeacherPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        welcomeLabel = new JLabel("Bienvenido, Profesor");
        topPanel.add(welcomeLabel);

        logoutButton = new JButton("Cerrar Sesión");
        logoutButton.addActionListener(e -> mainFrame.logout());
        topPanel.add(logoutButton);

        add(topPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Mis Learning Paths", createMyLPPanel());
        tabbedPane.addTab("Crear Actividades", createActivitiesPanel());
        tabbedPane.addTab("Respuestas Estudiantes", createStudentResponsesPanel());
        tabbedPane.addTab("Actividad Anual", createYearActivityTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void updateData(User user) {
        this.currentUser = user;
        if (user instanceof Teacher) {
            welcomeLabel.setText("Bienvenido, " + user.getName() + " (Profesor)");
        } else {
            welcomeLabel.setText("Bienvenido, " + user.getName());
        }
        loadMyLearningPaths();
        updateYearActivity();
    }

    private JPanel createMyLPPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        myLPTableModel = new DefaultTableModel(new String[]{"Título", "Descripción", "Dificultad", "Duración"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        myLPTable = new JTable(myLPTableModel);
        panel.add(new JScrollPane(myLPTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadMyLearningPaths() {
        if (currentUser == null) return;
        myLPTableModel.setRowCount(0);
        // Antes filtrábamos por Teacher. Ahora sigue igual si es un profesor. 
        // Si no es profesor, igual puede tener LPs creados (teóricamente), o se deja vacío.
        if (currentUser.getRole() == Role.TEACHER) {
            Teacher t = (Teacher) currentUser;
            for (LearningPath lp : mainFrame.getLearningPaths()) {
                if (lp.getCreator().equals(t)) {
                    myLPTableModel.addRow(new Object[]{
                            lp.getTitle(),
                            lp.getDescription(),
                            lp.getDifficultyLevel(),
                            lp.getDuration()
                    });
                }
            }
        } else {
            // Si el usuario no es profesor, no cargamos LP propios. (O si se quiere, se pueden mostrar todos)
        }
    }

    private JPanel createActivitiesPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        JButton addLPBtn = new JButton("Crear Learning Path");
        addLPBtn.addActionListener(e -> showCreateLearningPathDialog());

        JButton addQuizBtn = new JButton("Crear Quiz");
        addQuizBtn.addActionListener(e -> showCreateQuizDialog());

        JButton addSurveyBtn = new JButton("Crear Survey");
        addSurveyBtn.addActionListener(e -> showCreateSurveyDialog());

        JButton addOpenEndedExamBtn = new JButton("Crear Examen Preguntas Abiertas");
        addOpenEndedExamBtn.addActionListener(e -> showCreateOpenEndedExamDialog());

        JButton addResourceReviewBtn = new JButton("Crear Resource Review");
        addResourceReviewBtn.addActionListener(e -> showCreateResourceReviewDialog());

        panel.add(addLPBtn);
        panel.add(addQuizBtn);
        panel.add(addSurveyBtn);
        panel.add(addOpenEndedExamBtn);
        panel.add(addResourceReviewBtn);

        return panel;
    }

    private void showCreateLearningPathDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Crear Learning Path", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(6,2,5,5));

        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JTextField objField = new JTextField();
        JComboBox<Integer> diffBox = new JComboBox<>(new Integer[]{1,2,3,4,5});
        JButton createBtn = new JButton("Crear");
        JButton cancelBtn = new JButton("Cancelar");

        dialog.add(new JLabel("Título:"));
        dialog.add(titleField);
        dialog.add(new JLabel("Descripción:"));
        dialog.add(descField);
        dialog.add(new JLabel("Objetivos:"));
        dialog.add(objField);
        dialog.add(new JLabel("Dificultad (1-5):"));
        dialog.add(diffBox);
        dialog.add(new JLabel(""));
        dialog.add(new JLabel(""));

        dialog.add(createBtn);
        dialog.add(cancelBtn);

        createBtn.addActionListener(ev -> {
            String title = titleField.getText().trim();
            String description = descField.getText().trim();
            String objectives = objField.getText().trim();
            int difficulty = (int) diffBox.getSelectedItem();

            if (title.isEmpty() || description.isEmpty() || objectives.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Complete todos los campos");
                return;
            }

            if (!(currentUser instanceof Teacher)) {
                JOptionPane.showMessageDialog(dialog, "Solo un profesor puede crear un Learning Path.");
                return;
            }

            Teacher t = (Teacher) currentUser;
            LearningPath lp = new LearningPath(title, description, objectives, difficulty, t);
            try {
                mainFrame.getLearningPaths().add(lp);
                mainFrame.saveAllData();
                mainFrame.reloadAllData();
                this.updateData(mainFrame.getCurrentUser());

                loadMyLearningPaths();
                JOptionPane.showMessageDialog(dialog, "Learning Path creado exitosamente.");
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al crear Learning Path: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(ev -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void showCreateQuizDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Crear Quiz", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        if (!(currentUser instanceof Teacher)) {
            JOptionPane.showMessageDialog(dialog, "Solo un profesor puede crear un Quiz.");
            dialog.dispose();
            return;
        }

        Teacher t = (Teacher) currentUser;
        List<LearningPath> teacherLPs = getTeacherLPs(t);
        if (teacherLPs.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "No tiene Learning Paths creados. Cree uno antes de agregar actividades.");
            dialog.dispose();
            return;
        }

        JPanel form = new JPanel(new GridLayout(8, 2, 5, 5));
        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JTextField objField = new JTextField();
        JComboBox<Integer> diffBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        JTextField durField = new JTextField("60");
        JCheckBox mandatoryCheck = new JCheckBox("Obligatorio");
        JTextField passingScoreField = new JTextField("70");

        JComboBox<LearningPath> lpComboBox = createLPComboBox(teacherLPs);

        form.add(new JLabel("Learning Path:"));
        form.add(lpComboBox);
        form.add(new JLabel("Título:"));
        form.add(titleField);
        form.add(new JLabel("Descripción:"));
        form.add(descField);
        form.add(new JLabel("Objetivo:"));
        form.add(objField);
        form.add(new JLabel("Dificultad:"));
        form.add(diffBox);
        form.add(new JLabel("Duración (min):"));
        form.add(durField);
        form.add(new JLabel("Obligatorio:"));
        form.add(mandatoryCheck);
        form.add(new JLabel("Puntuación mínima (%):"));
        form.add(passingScoreField);

        dialog.add(form, BorderLayout.NORTH);

        DefaultListModel<Question> questionListModel = new DefaultListModel<>();
        JList<Question> questionList = new JList<>(questionListModel);
        questionList.setVisibleRowCount(5);
        JScrollPane questionScrollPane = new JScrollPane(questionList);

        JButton addQuestionBtn = new JButton("Añadir Pregunta");
        addQuestionBtn.addActionListener(ev -> {
            Question q = createQuestionDialog();
            if (q != null) questionListModel.addElement(q);
        });

        JPanel questionPanel = new JPanel(new BorderLayout());
        questionPanel.setBorder(BorderFactory.createTitledBorder("Preguntas del Quiz"));
        questionPanel.add(questionScrollPane, BorderLayout.CENTER);
        questionPanel.add(addQuestionBtn, BorderLayout.SOUTH);

        dialog.add(questionPanel, BorderLayout.CENTER);

        JButton createBtn = new JButton("Crear Quiz");
        createBtn.addActionListener(ev -> {
            String title = titleField.getText().trim();
            String desc = descField.getText().trim();
            String obj = objField.getText().trim();
            int diff = (int) diffBox.getSelectedItem();
            int dur;
            try {
                dur = Integer.parseInt(durField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Duración inválida.");
                return;
            }
            boolean mandatory = mandatoryCheck.isSelected();
            double passingScore;
            try {
                passingScore = Double.parseDouble(passingScoreField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Puntuación mínima inválida.");
                return;
            }

            LearningPath selectedLP = (LearningPath) lpComboBox.getSelectedItem();
            if (selectedLP == null) {
                JOptionPane.showMessageDialog(dialog, "Seleccione un Learning Path.");
                return;
            }

            List<Question> questions = new ArrayList<>();
            for (int i = 0; i < questionListModel.size(); i++) {
                questions.add(questionListModel.getElementAt(i));
            }

            if (questions.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Debe añadir al menos una pregunta al Quiz.");
                return;
            }

            Quiz quiz = new Quiz(title, desc, obj, diff, dur, mandatory, questions, passingScore);
            selectedLP.addActivity(quiz);
            mainFrame.saveAllData();
            mainFrame.reloadAllData();
            this.updateData(mainFrame.getCurrentUser());

            JOptionPane.showMessageDialog(dialog, "Quiz creado exitosamente en el Learning Path: " + selectedLP.getTitle());
            loadMyLearningPaths();
            dialog.dispose();
        });

        JPanel createBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        createBtnPanel.add(createBtn);

        dialog.add(createBtnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showCreateSurveyDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Crear Survey", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        if (!(currentUser instanceof Teacher)) {
            JOptionPane.showMessageDialog(dialog, "Solo un profesor puede crear una Survey.");
            dialog.dispose();
            return;
        }

        Teacher t = (Teacher) currentUser;
        List<LearningPath> teacherLPs = getTeacherLPs(t);
        if (teacherLPs.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "No tiene Learning Paths creados. Cree uno antes de agregar actividades.");
            dialog.dispose();
            return;
        }

        JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));
        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JTextField objField = new JTextField();
        JComboBox<Integer> diffBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        JTextField durField = new JTextField("30");
        JCheckBox mandatoryCheck = new JCheckBox("Obligatorio");

        JComboBox<LearningPath> lpComboBox = createLPComboBox(teacherLPs);

        form.add(new JLabel("Learning Path:"));
        form.add(lpComboBox);
        form.add(new JLabel("Título:"));
        form.add(titleField);
        form.add(new JLabel("Descripción:"));
        form.add(descField);
        form.add(new JLabel("Objetivo:"));
        form.add(objField);
        form.add(new JLabel("Dificultad:"));
        form.add(diffBox);
        form.add(new JLabel("Duración (min):"));
        form.add(durField);
        form.add(new JLabel("Obligatorio:"));
        form.add(mandatoryCheck);

        dialog.add(form, BorderLayout.NORTH);

        DefaultListModel<SurveyQuestion> questionListModel = new DefaultListModel<>();
        JList<SurveyQuestion> questionList = new JList<>(questionListModel);
        questionList.setVisibleRowCount(5);
        JScrollPane questionScrollPane = new JScrollPane(questionList);

        JButton addQuestionBtn = new JButton("Añadir Pregunta");
        addQuestionBtn.addActionListener(ev -> {
            SurveyQuestion sq = createSurveyQuestionDialog();
            if (sq != null) questionListModel.addElement(sq);
        });

        JPanel questionPanel = new JPanel(new BorderLayout());
        questionPanel.setBorder(BorderFactory.createTitledBorder("Preguntas del Survey"));
        questionPanel.add(questionScrollPane, BorderLayout.CENTER);
        questionPanel.add(addQuestionBtn, BorderLayout.SOUTH);

        dialog.add(questionPanel, BorderLayout.CENTER);

        JButton createBtn = new JButton("Crear Survey");
        createBtn.addActionListener(ev -> {
            String title = titleField.getText().trim();
            String desc = descField.getText().trim();
            String obj = objField.getText().trim();
            int diff = (int) diffBox.getSelectedItem();
            int dur;
            try {
                dur = Integer.parseInt(durField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Duración inválida.");
                return;
            }
            boolean mandatory = mandatoryCheck.isSelected();

            List<SurveyQuestion> questions = new ArrayList<>();
            for (int i = 0; i < questionListModel.size(); i++) {
                questions.add(questionListModel.getElementAt(i));
            }

            if (questions.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Debe añadir al menos una pregunta.");
                return;
            }

            LearningPath selectedLP = (LearningPath) lpComboBox.getSelectedItem();
            if (selectedLP == null) {
                JOptionPane.showMessageDialog(dialog, "Seleccione un Learning Path.");
                return;
            }

            Survey survey = new Survey(title, desc, obj, diff, dur, mandatory);
            for (SurveyQuestion sq : questions) {
                survey.addSurveyQuestion(sq);
            }
            selectedLP.addActivity(survey);
            mainFrame.saveAllData();
            mainFrame.reloadAllData();
            this.updateData(mainFrame.getCurrentUser());

            JOptionPane.showMessageDialog(dialog, "Survey creado exitosamente en el Learning Path: " + selectedLP.getTitle());
            loadMyLearningPaths();
            dialog.dispose();
        });
        JPanel createBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        createBtnPanel.add(createBtn);

        dialog.add(createBtnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showCreateOpenEndedExamDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Crear Examen de Preguntas Abiertas", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        if (!(currentUser instanceof Teacher)) {
            JOptionPane.showMessageDialog(dialog, "Solo un profesor puede crear un Examen de Preguntas Abiertas.");
            dialog.dispose();
            return;
        }

        Teacher t = (Teacher) currentUser;
        List<LearningPath> teacherLPs = getTeacherLPs(t);
        if (teacherLPs.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "No tiene Learning Paths creados. Cree uno antes de agregar actividades.");
            dialog.dispose();
            return;
        }

        JPanel form = new JPanel(new GridLayout(7, 2, 5, 5));
        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JTextField objField = new JTextField();
        JComboBox<Integer> diffBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        JTextField durField = new JTextField("60");
        JCheckBox mandatoryCheck = new JCheckBox("Obligatorio");

        JComboBox<LearningPath> lpComboBox = createLPComboBox(teacherLPs);

        form.add(new JLabel("Learning Path:"));
        form.add(lpComboBox);
        form.add(new JLabel("Título:"));
        form.add(titleField);
        form.add(new JLabel("Descripción:"));
        form.add(descField);
        form.add(new JLabel("Objetivo:"));
        form.add(objField);
        form.add(new JLabel("Dificultad (1-5):"));
        form.add(diffBox);
        form.add(new JLabel("Duración (min):"));
        form.add(durField);
        form.add(new JLabel("Obligatorio:"));
        form.add(mandatoryCheck);

        dialog.add(form, BorderLayout.NORTH);

        DefaultListModel<String> questionListModel = new DefaultListModel<>();
        JList<String> questionList = new JList<>(questionListModel);
        JScrollPane questionScroll = new JScrollPane(questionList);
        JButton addQuestionBtn = new JButton("Añadir Pregunta");
        addQuestionBtn.addActionListener(ev -> {
            String qText = JOptionPane.showInputDialog(dialog, "Texto de la pregunta:");
            if (qText != null && !qText.trim().isEmpty()) {
                questionListModel.addElement(qText.trim());
            }
        });
        JPanel questionPanel = new JPanel(new BorderLayout());
        questionPanel.setBorder(BorderFactory.createTitledBorder("Preguntas Abiertas"));
        questionPanel.add(questionScroll, BorderLayout.CENTER);
        questionPanel.add(addQuestionBtn, BorderLayout.SOUTH);

        dialog.add(questionPanel, BorderLayout.CENTER);

        JPanel instructionsPanel = new JPanel(new BorderLayout());
        instructionsPanel.setBorder(BorderFactory.createTitledBorder("Instrucciones para el Estudiante"));
        JTextArea instructionsArea = new JTextArea(5, 40);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        JScrollPane instructionsScroll = new JScrollPane(instructionsArea);
        instructionsPanel.add(instructionsScroll, BorderLayout.CENTER);

        dialog.add(instructionsPanel, BorderLayout.EAST);

        JButton createBtn = new JButton("Crear Examen");
        createBtn.addActionListener(ev -> {
            String title = titleField.getText().trim();
            String desc = descField.getText().trim();
            String obj = objField.getText().trim();
            int diff;
            try {
                diff = (int) diffBox.getSelectedItem();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Dificultad inválida.");
                return;
            }
            int dur;
            try {
                dur = Integer.parseInt(durField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Duración inválida.");
                return;
            }
            boolean mandatory = mandatoryCheck.isSelected();

            if (title.isEmpty() || desc.isEmpty() || obj.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Complete todos los campos obligatorios.");
                return;
            }

            List<OpenEndedQuestion> openEndedQuestions = new ArrayList<>();
            for (int i = 0; i < questionListModel.size(); i++) {
                openEndedQuestions.add(new OpenEndedQuestion(questionListModel.getElementAt(i)));
            }
            if (openEndedQuestions.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Debe añadir al menos una pregunta.");
                return;
            }

            String instructionsText = instructionsArea.getText().trim();
            String instructionsFileName = "data/instructions_" + title.replaceAll("\\s+", "_") + ".txt";
            try {
                Files.write(Paths.get(instructionsFileName), instructionsText.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ioEx) {
                JOptionPane.showMessageDialog(dialog, "No se pudo guardar el archivo de instrucciones: " + ioEx.getMessage());
                return;
            }

            Set<ActivityType> types = new HashSet<>(Collections.singletonList(ActivityType.EXAMEN));
            OpenEndedExam exam = new OpenEndedExam(title, desc, obj, diff, dur, mandatory, types, openEndedQuestions);
            exam.setInstructionsFile(instructionsFileName);

            LearningPath selectedLP = (LearningPath) lpComboBox.getSelectedItem();
            if (selectedLP == null) {
                JOptionPane.showMessageDialog(dialog, "Seleccione un Learning Path.");
                return;
            }

            selectedLP.addActivity(exam);
            mainFrame.saveAllData();
            mainFrame.reloadAllData();
            this.updateData(mainFrame.getCurrentUser());

            JOptionPane.showMessageDialog(dialog, "Examen de Preguntas Abiertas creado exitosamente en el Learning Path: " + selectedLP.getTitle());
            loadMyLearningPaths();
            dialog.dispose();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(createBtn);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showCreateResourceReviewDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Crear Resource Review", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        if (!(currentUser instanceof Teacher)) {
            JOptionPane.showMessageDialog(dialog, "Solo un profesor puede crear un Resource Review.");
            dialog.dispose();
            return;
        }

        Teacher t = (Teacher) currentUser;
        List<LearningPath> teacherLPs = getTeacherLPs(t);
        if (teacherLPs.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "No tiene Learning Paths creados. Cree uno antes de agregar actividades.");
            dialog.dispose();
            return;
        }

        JPanel form = new JPanel(new GridLayout(8, 2, 5, 5));
        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JTextField objField = new JTextField();
        JComboBox<Integer> diffBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        JTextField durField = new JTextField("30");
        JCheckBox mandatoryCheck = new JCheckBox("Obligatorio");
        JTextField resourceLinkField = new JTextField();
        JTextArea instructionsArea = new JTextArea(3, 20);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);

        JComboBox<LearningPath> lpComboBox = createLPComboBox(teacherLPs);

        form.add(new JLabel("Learning Path:"));
        form.add(lpComboBox);
        form.add(new JLabel("Título:"));
        form.add(titleField);
        form.add(new JLabel("Descripción:"));
        form.add(descField);
        form.add(new JLabel("Objetivo:"));
        form.add(objField);
        form.add(new JLabel("Dificultad:"));
        form.add(diffBox);
        form.add(new JLabel("Duración (min):"));
        form.add(durField);
        form.add(new JLabel("Obligatorio:"));
        form.add(mandatoryCheck);
        form.add(new JLabel("Resource Link:"));
        form.add(resourceLinkField);

        dialog.add(form, BorderLayout.NORTH);

        JPanel instructionsPanel = new JPanel(new BorderLayout());
        instructionsPanel.setBorder(BorderFactory.createTitledBorder("Instrucciones para el Estudiante"));
        JScrollPane instructionsScroll = new JScrollPane(instructionsArea);
        instructionsPanel.add(instructionsScroll, BorderLayout.CENTER);

        dialog.add(instructionsPanel, BorderLayout.CENTER);

        JButton createBtn = new JButton("Crear Resource Review");
        createBtn.addActionListener(ev -> {
            String title = titleField.getText().trim();
            String desc = descField.getText().trim();
            String obj = objField.getText().trim();
            int diff;
            try {
                diff = (int) diffBox.getSelectedItem();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Dificultad inválida.");
                return;
            }
            int dur;
            try {
                dur = Integer.parseInt(durField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Duración inválida.");
                return;
            }
            boolean mandatory = mandatoryCheck.isSelected();
            String resourceLink = resourceLinkField.getText().trim();
            String instructions = instructionsArea.getText().trim();

            if (title.isEmpty() || desc.isEmpty() || obj.isEmpty() || resourceLink.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Complete todos los campos obligatorios.");
                return;
            }

            LearningPath selectedLP = (LearningPath) lpComboBox.getSelectedItem();
            if (selectedLP == null) {
                JOptionPane.showMessageDialog(dialog, "Seleccione un Learning Path.");
                return;
            }

            ResourceReview rr = new ResourceReview(title, desc, obj, diff, dur, mandatory, resourceLink, instructions);
            selectedLP.addActivity(rr);
            mainFrame.saveAllData();
            mainFrame.reloadAllData();
            this.updateData(mainFrame.getCurrentUser());

            JOptionPane.showMessageDialog(dialog, "Resource Review creado exitosamente en el Learning Path: " + selectedLP.getTitle());
            loadMyLearningPaths();
            dialog.dispose();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(createBtn);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Ahora cualquier usuario puede ver las respuestas de los estudiantes.
     * No se filtra por profesor, se listan todos los Learning Paths.
     */
    private JPanel createStudentResponsesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        List<LearningPath> allLPs = mainFrame.getLearningPaths();
        if (allLPs.isEmpty()) {
            panel.add(new JLabel("No hay Learning Paths disponibles."), BorderLayout.CENTER);
            return panel;
        }

        JComboBox<LearningPath> lpComboBox = createLPComboBox(allLPs);
        DefaultListModel<Student> studentListModel = new DefaultListModel<>();
        JList<Student> studentList = new JList<>(studentListModel);
        studentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        JScrollPane detailsScroll = new JScrollPane(detailsArea);

        JButton reloadButton = new JButton("Recargar");
        reloadButton.addActionListener(e -> {
            mainFrame.reloadAllData();
            List<LearningPath> updatedLPs = mainFrame.getLearningPaths();
            lpComboBox.removeAllItems();
            for (LearningPath lp : updatedLPs) {
                lpComboBox.addItem(lp);
            }
            studentListModel.clear();
            detailsArea.setText("");
        });

        lpComboBox.addActionListener(e -> {
            studentListModel.clear();
            LearningPath selectedLP = (LearningPath) lpComboBox.getSelectedItem();
            if (selectedLP != null) {
                for (Progress pr : mainFrame.getProgresses()) {
                    if (pr.getLearningPath().equals(selectedLP)) {
                        studentListModel.addElement(pr.getStudent());
                    }
                }
            }
        });

        studentList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Student selectedStudent = studentList.getSelectedValue();
                LearningPath selectedLP = (LearningPath) lpComboBox.getSelectedItem();
                if (selectedStudent != null && selectedLP != null) {
                    Progress pr = mainFrame.findProgress(selectedStudent, selectedLP);
                    if (pr != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Estudiante: ").append(selectedStudent.getName()).append("\n");
                        sb.append("Learning Path: ").append(selectedLP.getTitle()).append("\n\n");
                        sb.append("Actividades:\n");

                        for (Activity a : selectedLP.getActivities()) {
                            ActivityStatus status = pr.getActivityStatus(a);
                            sb.append("- ").append(a.getTitle()).append(" (").append(a.getType()).append("): ").append(status).append("\n");

                            if (a instanceof Quiz && (status == ActivityStatus.COMPLETED || status == ActivityStatus.FAILED)) {
                                double score = calculateQuizScore((Quiz) a, pr);
                                sb.append("  Puntaje del Quiz: ").append(String.format("%.2f", score)).append("%\n");
                            }

                            if (a instanceof Survey && status == ActivityStatus.SUBMITTED) {
                                Survey s = (Survey) a;
                                SurveyResponse resp = pr.getSurveyResponse(s);
                                if (resp != null && resp.getAnswers() != null && !resp.getAnswers().isEmpty()) {
                                    sb.append("  Respuestas del Survey:\n");
                                    for (Map.Entry<String, String> entry : resp.getAnswers().entrySet()) {
                                        sb.append("    Pregunta: ").append(entry.getKey()).append("\n");
                                        sb.append("    Respuesta: ").append(entry.getValue()).append("\n");
                                    }
                                } else {
                                    sb.append("  No hay respuestas registradas para esta Survey.\n");
                                }
                            }

                            if (a instanceof OpenEndedExam && status == ActivityStatus.SUBMITTED) {
                                OpenEndedExam exam = (OpenEndedExam) a;
                                OpenEndedResponse resp = pr.getExamResponse(exam);
                                if (resp != null && resp.getAnswers() != null && !resp.getAnswers().isEmpty()) {
                                    sb.append("  Respuestas del Examen:\n");
                                    for (Map.Entry<String, String> entry : resp.getAnswers().entrySet()) {
                                        sb.append("    Pregunta: ").append(entry.getKey()).append("\n");
                                        sb.append("    Respuesta: ").append(entry.getValue()).append("\n");
                                    }
                                } else {
                                    sb.append("  No hay respuestas registradas para este Examen.\n");
                                }
                            }
                        }

                        detailsArea.setText(sb.toString());
                    } else {
                        detailsArea.setText("Sin progreso para este estudiante.");
                    }
                }
            }
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Seleccione un Learning Path:"));
        topPanel.add(lpComboBox);
        topPanel.add(reloadButton);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(studentList), detailsScroll);
        split.setDividerLocation(200);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createYearActivityTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Año:"));
        JTextField yearField = new JTextField(Integer.toString(currentYear), 5);
        topPanel.add(yearField);
        JButton reloadBtn = new JButton("Cargar");
        reloadBtn.addActionListener(e -> {
            try {
                int newYear = Integer.parseInt(yearField.getText().trim());
                if (newYear < 1900 || newYear > LocalDate.now().getYear()) {
                    throw new NumberFormatException();
                }
                currentYear = newYear;
                updateYearActivity();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Año inválido.");
            }
        });
        topPanel.add(reloadBtn);

        panel.add(topPanel, BorderLayout.NORTH);

        yearActivityPanelContainer = new JPanel(new BorderLayout());
        panel.add(yearActivityPanelContainer, BorderLayout.CENTER);

        return panel;
    }

    private void updateYearActivity() {
        if (currentUser != null) {
            Map<LocalDate, Integer> data = mainFrame.getDailyActivityCountForYear(currentYear);
            if (yearActivityPanel != null) {
                yearActivityPanelContainer.remove(yearActivityPanel);
            }
            yearActivityPanel = new YearActivityPanel(currentYear, data);
            yearActivityPanelContainer.add(yearActivityPanel, BorderLayout.CENTER);
            yearActivityPanelContainer.revalidate();
            yearActivityPanelContainer.repaint();
        }
    }

    private List<LearningPath> getTeacherLPs(Teacher t) {
        List<LearningPath> teacherLPs = new ArrayList<>();
        for (LearningPath lp : mainFrame.getLearningPaths()) {
            if (lp.getCreator().equals(t)) {
                teacherLPs.add(lp);
            }
        }
        return teacherLPs;
    }

    private JComboBox<LearningPath> createLPComboBox(List<LearningPath> teacherLPs) {
        JComboBox<LearningPath> lpComboBox = new JComboBox<>(teacherLPs.toArray(new LearningPath[0]));
        lpComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LearningPath) {
                    LearningPath lp = (LearningPath) value;
                    setText(lp.getTitle());
                }
                return c;
            }
        });
        return lpComboBox;
    }

    private Question createQuestionDialog() {
        JDialog qd = new JDialog(SwingUtilities.getWindowAncestor(this), "Crear Pregunta para Quiz", Dialog.ModalityType.APPLICATION_MODAL);
        qd.setSize(400, 400);
        qd.setLocationRelativeTo(this);
        qd.setLayout(new GridLayout(8, 2, 5, 5));

        JTextField qText = new JTextField();
        JTextField opt1 = new JTextField();
        JTextField opt2 = new JTextField();
        JTextField opt3 = new JTextField();
        JTextField opt4 = new JTextField();
        JTextField correctIndex = new JTextField("1");
        JTextField explanation = new JTextField();

        qd.add(new JLabel("Texto de la pregunta:"));
        qd.add(qText);
        qd.add(new JLabel("Opción 1:"));
        qd.add(opt1);
        qd.add(new JLabel("Opción 2:"));
        qd.add(opt2);
        qd.add(new JLabel("Opción 3:"));
        qd.add(opt3);
        qd.add(new JLabel("Opción 4:"));
        qd.add(opt4);
        qd.add(new JLabel("Índice correcta (1-4):"));
        qd.add(correctIndex);
        qd.add(new JLabel("Explicación:"));
        qd.add(explanation);

        final Question[] createdQ = {null};
        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancelar");
        okBtn.addActionListener(e -> {
            String qt = qText.getText().trim();
            String[] options = new String[]{
                    opt1.getText().trim(),
                    opt2.getText().trim(),
                    opt3.getText().trim(),
                    opt4.getText().trim()
            };
            int ci;
            try {
                ci = Integer.parseInt(correctIndex.getText().trim()) - 1;
                if (ci < 0 || ci > 3) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(qd, "Índice correcta inválido. Debe ser entre 1 y 4.");
                return;
            }
            String exp = explanation.getText().trim();
            if (qt.isEmpty() || options[0].isEmpty() || options[1].isEmpty() || options[2].isEmpty() || options[3].isEmpty()) {
                JOptionPane.showMessageDialog(qd, "Todos los campos son obligatorios.");
                return;
            }
            createdQ[0] = new Question(qt, options, ci, exp);
            qd.dispose();
        });
        cancelBtn.addActionListener(e -> {
            createdQ[0] = null;
            qd.dispose();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        qd.add(btnPanel);

        qd.setVisible(true);
        return createdQ[0];
    }

    private SurveyQuestion createSurveyQuestionDialog() {
        JDialog qd = new JDialog(SwingUtilities.getWindowAncestor(this), "Crear Pregunta de Survey", Dialog.ModalityType.APPLICATION_MODAL);
        qd.setSize(400, 200);
        qd.setLocationRelativeTo(this);
        qd.setLayout(new GridLayout(3, 2, 5, 5));

        JTextField qText = new JTextField();

        qd.add(new JLabel("Texto de la pregunta:"));
        qd.add(qText);

        final SurveyQuestion[] createdSQ = {null};
        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancelar");
        okBtn.addActionListener(e -> {
            String qt = qText.getText().trim();
            if (qt.isEmpty()) {
                JOptionPane.showMessageDialog(qd, "El texto de la pregunta es obligatorio.");
                return;
            }
            createdSQ[0] = new SurveyQuestion(qt);
            qd.dispose();
        });
        cancelBtn.addActionListener(e -> {
            createdSQ[0] = null;
            qd.dispose();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        qd.add(btnPanel);

        qd.setVisible(true);
        return createdSQ[0];
    }

    private double calculateQuizScore(Quiz quiz, Progress pr) {
        List<Integer> studentAnswers = pr.getQuizResponses(quiz);
        if (studentAnswers == null) return 0.0;
        List<Question> qs = quiz.getQuestions();
        int correct = 0;
        for (int i = 0; i < qs.size(); i++) {
            if (studentAnswers.get(i) == qs.get(i).getCorrectOptionIndex()) {
                correct++;
            }
        }
        return ((double) correct / qs.size()) * 100.0;
    }
}




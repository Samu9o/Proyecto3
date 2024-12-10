package src.com.learningpath.gui;

import java.awt.CardLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import src.com.learningpath.LearningPath;
import src.com.learningpath.Progress;
import src.com.learningpath.activities.Activity;
import src.com.learningpath.activities.ActivityStatus;
import src.com.learningpath.data.DataManager;
import src.com.learningpath.users.Role;
import src.com.learningpath.users.Student;
import src.com.learningpath.users.User;

public class MainFrame extends JFrame {
    private List<User> users;
    private List<LearningPath> learningPaths;
    private List<Progress> progresses;

    private User currentUser;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private LoginPanel loginPanel;
    private TeacherPanel teacherPanel;
    private StudentPanel studentPanel;

    public MainFrame() {
        super("Sistema de Gestión de Learning Paths");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        loadData();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        loginPanel = new LoginPanel(this);
        teacherPanel = new TeacherPanel(this);
        studentPanel = new StudentPanel(this);

        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(teacherPanel, "TEACHER");
        mainPanel.add(studentPanel, "STUDENT");

        add(mainPanel);
        showLogin();
    }

    /**
     * Carga los datos desde DataManager.
     */
    private void loadData() {
        try {
            users = DataManager.loadUsers();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No se encontraron usuarios previos. Iniciando con lista vacía.");
            users = new ArrayList<>();
        }

        try {
            learningPaths = DataManager.loadLearningPaths();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No se encontraron Learning Paths previos. Iniciando con lista vacía.");
            learningPaths = new ArrayList<>();
        }

        try {
            progresses = DataManager.loadProgresses();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No se encontraron Progresos previos. Iniciando con lista vacía.");
            progresses = new ArrayList<>();
        }
    }

    /**
     * Recarga los datos desde DataManager.
     */
    public void reloadAllData() {
        loadData();
        
        System.out.println("Datos recargados exitosamente.");
        if (currentUser != null) {
            User newCurrent = null;
            for (User u : users) {
                if (u.getUsername().equals(currentUser.getUsername()) && u.getRole() == currentUser.getRole()) {
                    newCurrent = u;
                    break;
                }
            }
            currentUser = newCurrent;
        }

        // Actualizar la vista según el usuario actual
        if (currentUser != null) {
            if (currentUser.getRole() == Role.TEACHER) {
                teacherPanel.updateData(currentUser);
            } else {
                studentPanel.updateData(currentUser);
            }
        }
    }

    /**
     * Muestra la pantalla de login.
     */
    public void showLogin() {
        cardLayout.show(mainPanel, "LOGIN");
    }

    /**
     * Muestra la vista para profesores.
     */
    public void showTeacherView() {
        teacherPanel.updateData(currentUser);
        cardLayout.show(mainPanel, "TEACHER");
    }

    /**
     * Muestra la vista para estudiantes.
     */
    public void showStudentView() {
        studentPanel.updateData(currentUser);
        cardLayout.show(mainPanel, "STUDENT");
    }

    /**
     * Autentica a un usuario con nombre de usuario y contraseña.
     * 
     * @param username Nombre de usuario.
     * @param password Contraseña.
     * @return True si la autenticación es exitosa, false en caso contrario.
     */
    public boolean authenticate(String username, String password) {
        if (users == null) return false;
        for (User u : users) {
            if (u.getUsername().equals(username) && u.authenticate(password)) {
                currentUser = u;
                return true;
            }
        }
        return false;
    }

    /**
     * Cierra la sesión actual y muestra la pantalla de login.
     */
    public void logout() {
        currentUser = null;
        showLogin();
    }

    /**
     * Registra un nuevo usuario.
     * 
     * @param newUser El nuevo usuario a registrar.
     * @throws Exception Si el nombre de usuario ya existe.
     */
    public void registerUser(User newUser) throws Exception {
        for (User u : users) {
            if (u.getUsername().equals(newUser.getUsername())) {
                throw new Exception("El nombre de usuario ya existe.");
            }
        }
        users.add(newUser);
        saveAllData();
    }

    /**
     * Añade un nuevo Learning Path.
     * 
     * @param lp El Learning Path a añadir.
     * @throws Exception Si ocurre un error al guardar los datos.
     */
    public void addLearningPath(LearningPath lp) throws Exception {
        learningPaths.add(lp);
        saveAllData();
    }

    /**
     * Añade un nuevo Progress.
     * 
     * @param p El Progress a añadir.
     * @throws Exception Si ocurre un error al guardar los datos.
     */
    public void addProgress(Progress p) throws Exception {
        progresses.add(p);
        saveAllData();
    }

    /**
     * Busca el Progress de un estudiante en un Learning Path específico.
     * 
     * @param s  El estudiante.
     * @param lp El Learning Path.
     * @return El objeto Progress si se encuentra, null en caso contrario.
     */
    public Progress findProgress(Student s, LearningPath lp) {
        for (Progress p : progresses) {
            if (p.getStudent().equals(s) && p.getLearningPath().equals(lp)) {
                return p;
            }
        }
        return null;
    }
    public Map<LocalDate, Integer> getDailyActivityCountForYear(int year) {
        Map<LocalDate, Integer> dailyCount = new HashMap<>();

        // Recorremos todos los progresos
        for (Progress pr : progresses) {
            // Recorremos todas las actividades y sus estados
            for (Map.Entry<Activity, ActivityStatus> entry : pr.getActivityStatuses().entrySet()) {
                ActivityStatus status = entry.getValue();

                // Consideramos actividades COMPLETED o SUBMITTED como finalizadas
                if (status == ActivityStatus.COMPLETED || status == ActivityStatus.SUBMITTED) {
                    LocalDate completionDate = pr.getCompletionDate(entry.getKey());
                    if (completionDate != null && completionDate.getYear() == year) {
                        dailyCount.put(completionDate, dailyCount.getOrDefault(completionDate, 0) + 1);
                    }
                }
            }
        }

        return dailyCount;
    }


    /**
     * Guarda todos los datos utilizando DataManager.
     */
    public void saveAllData() {
        try {
            DataManager.saveUsers(users);
            DataManager.saveLearningPaths(learningPaths);
            DataManager.saveProgresses(progresses);
            System.out.println("Datos guardados exitosamente.");
        } catch (IOException e) {
            System.out.println("Error al guardar datos: " + e.getMessage());
        }
    }

    // Getters
    public User getCurrentUser() {
        return currentUser;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<LearningPath> getLearningPaths() {
        return learningPaths;
    }
    

    public List<Progress> getProgresses() {
        return progresses;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mf = new MainFrame();
            mf.setVisible(true);
        });
    }
}



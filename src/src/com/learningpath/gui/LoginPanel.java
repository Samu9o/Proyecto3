package src.com.learningpath.gui;

import src.com.learningpath.users.*;
import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private MainFrame mainFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel messageLabel;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx=0; gbc.gridy=0; gbc.anchor=GridBagConstraints.CENTER;
        add(new JLabel("Usuario:"), gbc);

        usernameField = new JTextField(20);
        gbc.gridy=1;
        add(usernameField, gbc);

        gbc.gridy=2;
        add(new JLabel("Contraseña:"), gbc);

        passwordField = new JPasswordField(20);
        gbc.gridy=3;
        add(passwordField, gbc);

        loginButton = new JButton("Iniciar Sesión");
        registerButton = new JButton("Registrarse");

        gbc.gridy=4;
        add(loginButton, gbc);
        gbc.gridy=5;
        add(registerButton, gbc);

        messageLabel = new JLabel("");
        messageLabel.setForeground(Color.RED);
        gbc.gridy=6;
        add(messageLabel, gbc);

        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> showRegisterDialog());
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (mainFrame.authenticate(username, password)) {
            User u = mainFrame.getCurrentUser();
            if (u.getRole() == Role.TEACHER) {
                mainFrame.showTeacherView();
            } else {
                mainFrame.showStudentView();
            }
        } else {
            messageLabel.setText("Credenciales incorrectas");
        }
    }

    private void showRegisterDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Registro", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(6,2));

        JTextField regUserField = new JTextField();
        JPasswordField regPassField = new JPasswordField();
        JPasswordField regPassConfirmField = new JPasswordField();
        JTextField nameField = new JTextField();

        String[] roles = {"Estudiante", "Profesor"};
        JComboBox<String> roleBox = new JComboBox<>(roles);

        dialog.add(new JLabel("Usuario:"));
        dialog.add(regUserField);
        dialog.add(new JLabel("Contraseña:"));
        dialog.add(regPassField);
        dialog.add(new JLabel("Confirmar Contraseña:"));
        dialog.add(regPassConfirmField);
        dialog.add(new JLabel("Nombre completo:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Rol:"));
        dialog.add(roleBox);

        JButton okButton = new JButton("Registrar");
        JButton cancelButton = new JButton("Cancelar");
        dialog.add(okButton);
        dialog.add(cancelButton);

        okButton.addActionListener(e -> {
            String user = regUserField.getText().trim();
            String pass = new String(regPassField.getPassword());
            String pass2 = new String(regPassConfirmField.getPassword());
            String nombre = nameField.getText().trim();
            String rol = (String) roleBox.getSelectedItem();

            if (user.isEmpty() || pass.isEmpty() || nombre.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Complete todos los campos");
                return;
            }

            if (!pass.equals(pass2)) {
                JOptionPane.showMessageDialog(dialog, "Las contraseñas no coinciden");
                return;
            }

            User newUser;
            if ("Profesor".equals(rol)) {
                newUser = new Teacher(user, pass, nombre);
            } else {
                newUser = new Student(user, pass, nombre);
            }

            try {
                mainFrame.registerUser(newUser);
                JOptionPane.showMessageDialog(dialog, "Usuario registrado exitosamente. Ahora puede iniciar sesión.");
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al registrar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }
}


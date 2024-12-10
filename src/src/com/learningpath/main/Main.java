package src.com.learningpath.main;

import javax.swing.SwingUtilities;
import src.com.learningpath.gui.MainFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mf = new MainFrame();
            mf.setVisible(true);
        });
    }
}

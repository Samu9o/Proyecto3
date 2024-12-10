package src.com.learningpath.gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.Year;
import java.util.Map;

/**
 * Panel que muestra un mapa de calor de actividades completadas durante un año.
 */
public class YearActivityPanel extends JPanel {
    private Map<LocalDate, Integer> dailyActivityCount;
    private int maxActivities;
    private int yearToShow;

    private static final int WEEKS = 53; // Máximo número de semanas en un año
    private static final int DAYS_PER_WEEK = 7;
    private static final int CELL_SIZE = 15; // Tamaño de cada celda en píxeles

    /**
     * Constructor de YearActivityPanel.
     *
     * @param yearToShow         El año a mostrar.
     * @param dailyActivityCount Mapa con la cantidad de actividades por día.
     */
    public YearActivityPanel(int yearToShow, Map<LocalDate, Integer> dailyActivityCount) {
        this.yearToShow = yearToShow;
        this.dailyActivityCount = dailyActivityCount;
        this.maxActivities = dailyActivityCount.values().stream().max(Integer::compareTo).orElse(0);
        setPreferredSize(new Dimension(WEEKS * CELL_SIZE + 100, DAYS_PER_WEEK * CELL_SIZE + 100));
        setBackground(Color.WHITE);
    }

    /**
     * Actualiza los datos del mapa de calor.
     *
     * @param yearToShow         El año a mostrar.
     * @param dailyActivityCount Mapa con la cantidad de actividades por día.
     */
    public void updateData(int yearToShow, Map<LocalDate, Integer> dailyActivityCount) {
        this.yearToShow = yearToShow;
        this.dailyActivityCount = dailyActivityCount;
        this.maxActivities = dailyActivityCount.values().stream().max(Integer::compareTo).orElse(0);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Dibujar títulos y leyendas
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Mapa de Calor de Actividades Completadas - " + yearToShow, 20, 20);

        // Dibujar leyenda
        drawLegend(g);

        // Dibujar el calendario
        drawCalendar(g);
    }

    /**
     * Dibuja el calendario con el mapa de calor.
     *
     * @param g El objeto Graphics.
     */
    private void drawCalendar(Graphics g) {
        // Determinar el primer día del año
        LocalDate start = LocalDate.of(yearToShow, 1, 1);
        int startDayOfWeek = start.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        // Convertir a 0=Sunday, 1=Monday, ..., 6=Saturday
        startDayOfWeek = (startDayOfWeek % 7);

        int dayCount = Year.isLeap(yearToShow) ? 366 : 365;

        // Dibujar cada día como una celda
        for (int i = 0; i < dayCount; i++) {
            LocalDate day = start.plusDays(i);
            int column = (i + startDayOfWeek) / DAYS_PER_WEEK;
            int row = (i + startDayOfWeek) % DAYS_PER_WEEK;

            int activities = dailyActivityCount.getOrDefault(day, 0);
            Color cellColor = getColorForValue(activities);

            int x = 50 + column * CELL_SIZE;
            int y = 50 + row * CELL_SIZE;

            // Dibujar la celda
            g.setColor(cellColor);
            g.fillRect(x, y, CELL_SIZE - 2, CELL_SIZE - 2);

            // Dibujar el borde de la celda
            g.setColor(Color.GRAY);
            g.drawRect(x, y, CELL_SIZE - 2, CELL_SIZE - 2);

            // Opcional: Mostrar el día en la celda si hay actividades
            if (activities > 0) {
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.PLAIN, 10));
                g.drawString(String.valueOf(activities), x + 2, y + 12);
            }
        }

        // Dibujar los nombres de los días de la semana
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        for (int i = 0; i < daysOfWeek.length; i++) {
            g.drawString(daysOfWeek[i], 10, 55 + i * CELL_SIZE);
        }
    }

    /**
     * Obtiene el color correspondiente a un valor de actividades.
     *
     * @param val La cantidad de actividades.
     * @return El color asignado.
     */
    private Color getColorForValue(int val) {
        if (maxActivities == 0) return new Color(235, 237, 240); // Gris claro

        float ratio = (float) val / (float) maxActivities;
        // Gradiente de verde claro a verde oscuro
        int rStart = 199, gStart = 236, bStart = 189;
        int rEnd = 34, gEnd = 139, bEnd = 34;

        int r = (int) (rStart + ratio * (rEnd - rStart));
        int g = (int) (gStart + ratio * (gEnd - gStart));
        int b = (int) (bStart + ratio * (bEnd - bStart));
        return new Color(r, g, b);
    }

    /**
     * Dibuja la leyenda del mapa de calor.
     *
     * @param g El objeto Graphics.
     */
    private void drawLegend(Graphics g) {
        int legendX = WEEKS * CELL_SIZE + 60;
        int legendY = 50;

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("Leyenda", legendX, legendY);

        // Crear una escala de colores
        int legendWidth = 20;
        int legendHeight = 100;
        for (int i = 0; i <= legendHeight; i++) {
            float ratio = (float) i / (float) legendHeight;
            // Invertir el ratio para que el color más oscuro esté en la parte superior
            ratio = 1.0f - ratio;
            int rStart = 199, gStart = 236, bStart = 189;
            int rEnd = 34, gEnd = 139, bEnd = 34;

            int r = (int) (rStart + ratio * (rEnd - rStart));
            int gColor = (int) (gStart + ratio * (gEnd - gStart));
            int b = (int) (bStart + ratio * (bEnd - bStart));
            g.setColor(new Color(r, gColor, b));
            g.fillRect(legendX, legendY + 10 + i, legendWidth, 1);
        }

        // Dibujar los textos de la leyenda
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.drawString(String.valueOf(maxActivities), legendX + legendWidth + 5, legendY + 15);
        g.drawString(String.valueOf(maxActivities / 2), legendX + legendWidth + 5, legendY + 55);
        g.drawString("0", legendX + legendWidth + 5, legendY + 105);
    }
}

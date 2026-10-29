package com.attendance;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
            new AttendanceFrame().setVisible(true);
        });
    }
}

class AttendanceFrame extends JFrame {
    static final Color NAVY = new Color(31, 47, 82);
    static final Color PURPLE = new Color(79, 70, 229);
    static final Color PURPLE_HOVER = new Color(67, 56, 202);
    static final Color BG = new Color(246, 247, 251);
    static final Color TEXT = new Color(35, 43, 60);
    static final Color MUTED = new Color(102, 112, 133);
    static final Color BORDER = new Color(224, 227, 235);
    static final Color HEADER = new Color(248, 249, 252);
    static final Color GREEN = new Color(22, 163, 74);
    static final Color GREEN_BG = new Color(220, 245, 230);
    static final Color ORANGE = new Color(217, 119, 6);
    static final Color ORANGE_BG = new Color(255, 239, 214);
    static final Color RED = new Color(220, 38, 38);
    static final Color RED_BG = new Color(255, 226, 226);
    static final Color GRAY = new Color(107, 114, 128);
    static final Color GRAY_BG = new Color(239, 241, 245);

    private final CsvData csv = new CsvData();
    private List<Student> students;
    private final JPanel content = new JPanel(new BorderLayout());
    private final JPanel sidebar = new JPanel(new BorderLayout());
    private final JPanel navPanel = new JPanel();
    private String currentPage = "Dashboard";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter prettyFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");

    AttendanceFrame() {
        students = csv.loadStudents();
        setTitle("Attendance Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(950, 620));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(buildSidebar(), BorderLayout.WEST);
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(28, 32, 22, 32));
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
        showDashboard();
    }

    private JPanel buildSidebar() {
        sidebar.removeAll();
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(240, 700));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(28, 16, 10, 16));

        JLabel title = new JLabel("Attendance");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Management System");
        sub.setForeground(new Color(185, 194, 214));
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(title);
        top.add(sub);
        top.add(Box.createVerticalStrut(28));

        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        rebuildNav();
        top.add(navPanel);
        sidebar.add(top, BorderLayout.NORTH);
        return sidebar;
    }

    private void rebuildNav() {
        navPanel.removeAll();
        String[][] items = {
                {"Dashboard", "⌂"},
                {"Students", "●"},
                {"Mark Attendance", "✓"},
                {"Student Report", "▣"}
        };
        for (String[] item : items) {
            navPanel.add(navButton(item[0], item[1]));
            navPanel.add(Box.createVerticalStrut(5));
        }
        navPanel.revalidate();
        navPanel.repaint();
    }

    private JButton navButton(String text, String icon) {
        SidebarButton b = new SidebarButton(text, icon, text.equals(currentPage));
        b.setLayout(new BorderLayout());
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setPreferredSize(new Dimension(208, 42));
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(0, 14, 0, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setFont(new Font("SansSerif", Font.BOLD, 17));

        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(iconLabel);
        left.add(Box.createHorizontalStrut(12));
        left.add(textLabel);
        b.add(left, BorderLayout.WEST);

        b.addActionListener(e -> {
            if (text.equals("Dashboard")) showDashboard();
            else if (text.equals("Students")) showStudents();
            else if (text.equals("Mark Attendance")) showMarkAttendance();
            else showStudentReport();
        });
        return b;
    }

    private void setPage(String page) {
        currentPage = page;
        rebuildNav();
        content.removeAll();
        content.revalidate();
        content.repaint();
    }

    private void showDashboard() {
        setPage("Dashboard");
        JPanel page = pagePanel();
        LocalDate todayDate = LocalDate.now();
        String todayString = todayDate.format(formatter);
        page.add(titleBlock("Dashboard", todayDate.format(prettyFormatter)), BorderLayout.NORTH);

        Map<String, String> today = csv.getAttendanceForDate(todayString);
        int present = 0, late = 0, absent = 0;
        for (String status : today.values()) {
            if (status.equals("Present")) present++;
            else if (status.equals("Late")) late++;
            else if (status.equals("Absent")) absent++;
        }

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);

        JPanel topRow = new JPanel(new BorderLayout(16, 0));
        topRow.setOpaque(false);

        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.add(statCard("Total students", students.size(), PURPLE, "●"));
        cards.add(statCard("Present today", present, GREEN, "✓"));
        cards.add(statCard("Late today", late, ORANGE, "◷"));
        cards.add(statCard("Absent today", absent, RED, "×"));
        topRow.add(cards, BorderLayout.CENTER);

        JButton pdf = primaryButton("Download Daily PDF");
        pdf.addActionListener(e -> {
            try {
                String path = csv.exportDailyPdf(todayString, students);
                JOptionPane.showMessageDialog(this, "PDF saved to:\n" + path);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not create PDF:\n" + ex.getMessage());
            }
        });
        topRow.add(pdf, BorderLayout.EAST);
        body.add(topRow, BorderLayout.NORTH);

        RoundedPanel tableCard = card(new BorderLayout(0, 12));
        tableCard.add(sectionTitle("Today's attendance & attendance score"), BorderLayout.NORTH);

        DefaultTableModel model = tableModel("Student ID", "Name", "Status", "Attendance Score");
        for (Student s : students) {
            model.addRow(new Object[]{
                    s.getId(),
                    s.getName(),
                    today.getOrDefault(s.getId(), "Not marked"),
                    csv.getAttendanceScore(s.getId())
            });
        }

        JTable table = styledTable(model);
        table.setRowHeight(50);
        table.getColumnModel().getColumn(2).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new ScoreRenderer());
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        body.add(tableCard, BorderLayout.CENTER);

        page.add(body, BorderLayout.CENTER);
        content.add(page, BorderLayout.CENTER);
    }

    private JPanel pagePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setOpaque(false);
        return p;
    }

    private JPanel titleBlock(String title, String subtitle) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(label(title, 28, Font.BOLD, TEXT));
        p.add(Box.createVerticalStrut(3));
        p.add(label(subtitle, 14, Font.PLAIN, MUTED));
        return p;
    }

    private JPanel statCard(String title, int number, Color color, String icon) {
        RoundedPanel p = card(new BorderLayout(0, 4));
        p.setBorder(new EmptyBorder(16, 18, 15, 18));
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(label(title, 13, Font.PLAIN, MUTED), BorderLayout.WEST);
        row.add(label(icon, 18, Font.BOLD, color), BorderLayout.EAST);
        p.add(row, BorderLayout.NORTH);
        p.add(label(String.valueOf(number), 34, Font.BOLD, color), BorderLayout.CENTER);
        return p;
    }

    private void showMarkAttendance() {
        setPage("Mark Attendance");
        JPanel page = pagePanel();
        page.add(titleBlock("Mark Attendance", "Marking attendance for the selected date"), BorderLayout.NORTH);

        RoundedPanel controls = card(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JTextField dateField = input(LocalDate.now().format(formatter), 12);
        JButton today = secondaryButton("Today");
        JButton load = secondaryButton("Load");
        JButton allPresent = secondaryButton("✓  Mark all present");
        JButton save = primaryButton("Save attendance");
        controls.add(label("Date (yyyy-MM-dd)", 13, Font.PLAIN, MUTED));
        controls.add(dateField);
        controls.add(today);
        controls.add(load);
        controls.add(allPresent);
        controls.add(Box.createHorizontalStrut(15));
        controls.add(save);
        page.add(controls, BorderLayout.NORTH);

        RoundedPanel tableCard = card(new BorderLayout(0, 0));
        DefaultTableModel model = tableModel("Student ID", "Name", "Status", "Comment");
        loadRowsForDate(model, dateField.getText());

        JTable table = styledTable(model);
        table.setRowHeight(50);
        table.getColumnModel().getColumn(2).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new CommentRenderer());
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        page.add(tableCard, BorderLayout.CENTER);

        page.add(label("Tip: click Status to cycle. When you choose Absent, enter a reason such as Sick or Family emergency.", 12, Font.PLAIN, MUTED), BorderLayout.SOUTH);

        today.addActionListener(e -> dateField.setText(LocalDate.now().format(formatter)));
        load.addActionListener(e -> loadRowsForDate(model, dateField.getText().trim()));
        allPresent.addActionListener(e -> {
            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt("Present", i, 2);
                model.setValueAt("", i, 3);
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0) return;

                if (col == 2) {
                    String old = String.valueOf(model.getValueAt(row, col));
                    String next = old.equals("Not marked") ? "Present"
                            : old.equals("Present") ? "Late"
                            : old.equals("Late") ? "Absent" : "Present";

                    if (next.equals("Absent")) {
                        String reason = JOptionPane.showInputDialog(
                                AttendanceFrame.this,
                                "Why is this student absent?",
                                "Absence comment",
                                JOptionPane.QUESTION_MESSAGE
                        );
                        if (reason == null || reason.trim().isEmpty()) {
                            JOptionPane.showMessageDialog(
                                    AttendanceFrame.this,
                                    "An absence comment is required."
                            );
                            return;
                        }
                        model.setValueAt(reason.trim(), row, 3);
                    } else if (old.equals("Absent")) {
                        model.setValueAt("", row, 3);
                    }

                    model.setValueAt(next, row, 2);
                    table.repaint();
                } else if (col == 3 && String.valueOf(model.getValueAt(row, 2)).equals("Absent")) {
                    String current = String.valueOf(model.getValueAt(row, 3));
                    String reason = JOptionPane.showInputDialog(
                            AttendanceFrame.this,
                            "Edit absence comment:",
                            current
                    );
                    if (reason != null && !reason.trim().isEmpty()) {
                        model.setValueAt(reason.trim(), row, 3);
                    }
                }
            }
        });

        save.addActionListener(e -> {
            String date = dateField.getText().trim();
            if (!isValidDate(date)) {
                JOptionPane.showMessageDialog(this, "Please use yyyy-MM-dd, for example 2026-10-08.");
                return;
            }

            Map<String, String> statuses = new LinkedHashMap<>();
            Map<String, String> comments = new LinkedHashMap<>();

            for (int i = 0; i < model.getRowCount(); i++) {
                String id = String.valueOf(model.getValueAt(i, 0));
                String status = String.valueOf(model.getValueAt(i, 2));
                String comment = String.valueOf(model.getValueAt(i, 3));

                if (status.equals("Absent") && comment.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Every absent student needs a comment.\nStudent: " + id);
                    return;
                }

                statuses.put(id, status);
                comments.put(id, comment);
            }

            csv.saveAttendance(date, students, statuses, comments);
            JOptionPane.showMessageDialog(this, "Attendance saved to data/attendance.csv");
        });

        content.add(page, BorderLayout.CENTER);
    }

    private void loadRowsForDate(DefaultTableModel model, String date) {
        Map<String, String> saved = csv.getAttendanceForDate(date);
        Map<String, String> comments = csv.getCommentsForDate(date);
        model.setRowCount(0);
        for (Student s : students) {
            model.addRow(new Object[]{
                    s.getId(),
                    s.getName(),
                    saved.getOrDefault(s.getId(), "Not marked"),
                    comments.getOrDefault(s.getId(), "")
            });
        }
    }

    private void showStudents() {
        setPage("Students");
        JPanel page = pagePanel();
        page.add(titleBlock("Students", "Add and manage students"), BorderLayout.NORTH);

        RoundedPanel controls = card(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JTextField id = input("S106", 8), name = input("Student name", 18);
        JButton add = primaryButton("+  Add student"), del = secondaryButton("Delete selected");
        controls.add(label("Student ID", 13, Font.PLAIN, MUTED));
        controls.add(id);
        controls.add(label("Name", 13, Font.PLAIN, MUTED));
        controls.add(name);
        controls.add(add);
        controls.add(del);
        page.add(controls, BorderLayout.NORTH);

        DefaultTableModel model = tableModel("Student ID", "Name", "Attendance Score");
        reloadStudentModel(model);
        JTable table = styledTable(model);
        table.setRowHeight(46);
        table.getColumnModel().getColumn(2).setCellRenderer(new ScoreRenderer());
        RoundedPanel area = card(new BorderLayout());
        area.add(new JScrollPane(table));
        page.add(area, BorderLayout.CENTER);

        add.addActionListener(e -> {
            String sid = id.getText().trim(), sn = name.getText().trim();
            if (sid.isEmpty() || sn.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter both Student ID and Name.");
                return;
            }
            for (Student s : students) {
                if (s.getId().equalsIgnoreCase(sid)) {
                    JOptionPane.showMessageDialog(this, "That Student ID already exists.");
                    return;
                }
            }
            csv.addStudent(new Student(sid, sn));
            students = csv.loadStudents();
            reloadStudentModel(model);
            id.setText("");
            name.setText("");
        });

        del.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a student first.");
                return;
            }
            String sid = String.valueOf(model.getValueAt(row, 0));
            if (JOptionPane.showConfirmDialog(this, "Delete student " + sid + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                csv.deleteStudent(sid);
                students = csv.loadStudents();
                reloadStudentModel(model);
            }
        });

        content.add(page, BorderLayout.CENTER);
    }

    private void reloadStudentModel(DefaultTableModel model) {
        model.setRowCount(0);
        for (Student s : students) {
            model.addRow(new Object[]{s.getId(), s.getName(), csv.getAttendanceScore(s.getId())});
        }
    }

    private void showStudentReport() {
        setPage("Student Report");
        JPanel page = pagePanel();
        page.add(titleBlock("Student Report", "Filter attendance by student"), BorderLayout.NORTH);

        RoundedPanel controls = card(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JComboBox<String> box = new JComboBox<>();
        box.addItem("All students");
        for (Student s : students) box.addItem(s.getId() + " - " + s.getName());
        JButton filter = primaryButton("Filter");
        JButton clear = secondaryButton("Clear");
        controls.add(label("Student", 13, Font.PLAIN, MUTED));
        controls.add(box);
        controls.add(filter);
        controls.add(clear);
        page.add(controls, BorderLayout.NORTH);

        DefaultTableModel model = tableModel("Date", "Student ID", "Name", "Status", "Comment");
        JTable table = styledTable(model);
        table.setRowHeight(46);
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new CommentRenderer());

        RoundedPanel area = card(new BorderLayout());
        area.add(new JScrollPane(table), BorderLayout.CENTER);
        page.add(area, BorderLayout.CENTER);

        Runnable refresh = () -> {
            String selectedStudent = String.valueOf(box.getSelectedItem());
            String studentId = selectedStudent.equals("All students") ? "" : selectedStudent.split(" - ", 2)[0];
            model.setRowCount(0);

            for (AttendanceRecord r : csv.loadAttendance()) {
                if (studentId.isEmpty() || r.getStudentId().equals(studentId)) {
                    model.addRow(new Object[]{
                            r.getDate(),
                            r.getStudentId(),
                            r.getStudentName(),
                            r.getStatus(),
                            r.getComment()
                    });
                }
            }
        };

        filter.addActionListener(e -> refresh.run());
        clear.addActionListener(e -> {
            box.setSelectedIndex(0);
            refresh.run();
        });
        refresh.run();
        content.add(page, BorderLayout.CENTER);
    }

    private RoundedPanel card(LayoutManager lm) {
        return new RoundedPanel(lm, Color.WHITE, 14);
    }

    private JLabel sectionTitle(String text) {
        return label(text, 18, Font.BOLD, TEXT);
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(color);
        return l;
    }

    private JTextField input(String text, int columns) {
        JTextField f = new JTextField(text, columns);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setForeground(TEXT);
        f.setBackground(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return f;
    }

    private JButton primaryButton(String text) {
        return button(text, PURPLE, PURPLE_HOVER, Color.WHITE);
    }

    private JButton secondaryButton(String text) {
        return button(text, new Color(238, 240, 245), new Color(220, 224, 232), TEXT);
    }

    private JButton button(String text, Color normal, Color hover, Color fg) {
        JButton b = new RoundedButton(text, normal, hover);
        b.setForeground(fg);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(9, 15, 9, 15));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private DefaultTableModel tableModel(String... cols) {
        return new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(new Font("SansSerif", Font.PLAIN, 14));
        t.setForeground(TEXT);
        t.setBackground(Color.WHITE);
        t.setGridColor(new Color(235, 237, 242));
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(new Color(242, 240, 255));
        t.setSelectionForeground(TEXT);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        t.getTableHeader().setForeground(MUTED);
        t.getTableHeader().setBackground(HEADER);
        return t;
    }

    private boolean isValidDate(String date) {
        try {
            LocalDate.parse(date, formatter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

class StatusRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
    StatusRenderer() {
        setOpaque(true);
        setHorizontalAlignment(SwingConstants.CENTER);
        setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean selected, boolean focused, int row, int column) {

        String status = String.valueOf(value);
        setText(status);
        setBorder(new EmptyBorder(6, 12, 6, 12));

        if ("Present".equals(status)) {
            setForeground(AttendanceFrame.GREEN);
            setBackground(AttendanceFrame.GREEN_BG);
        } else if ("Late".equals(status)) {
            setForeground(AttendanceFrame.ORANGE);
            setBackground(AttendanceFrame.ORANGE_BG);
        } else if ("Absent".equals(status)) {
            setForeground(AttendanceFrame.RED);
            setBackground(AttendanceFrame.RED_BG);
        } else {
            setForeground(AttendanceFrame.GRAY);
            setBackground(AttendanceFrame.GRAY_BG);
        }
        return this;
    }
}

class CommentRenderer extends DefaultTableCellRenderer {
    CommentRenderer() {
        setHorizontalAlignment(SwingConstants.LEFT);
        setForeground(AttendanceFrame.MUTED);
        setFont(new Font("SansSerif", Font.PLAIN, 13));
    }
}

class ScoreRenderer extends JProgressBar implements javax.swing.table.TableCellRenderer {
    ScoreRenderer() {
        super(0, 100);
        setStringPainted(true);
        setBorderPainted(false);
        setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean selected, boolean focused, int row, int column) {

        int score = 100;
        try { score = Integer.parseInt(String.valueOf(value)); } catch (Exception ignored) {}
        setValue(score);
        setString(score + "%");

        if (score >= 80) {
            setForeground(AttendanceFrame.GREEN);
        } else if (score >= 50) {
            setForeground(AttendanceFrame.ORANGE);
        } else {
            setForeground(AttendanceFrame.RED);
        }

        setBackground(new Color(235, 238, 244));
        return this;
    }
}

class SidebarButton extends JButton {
    private final boolean selected;

    SidebarButton(String text, String icon, boolean selected) {
        super();
        this.selected = selected;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = selected ? AttendanceFrame.PURPLE : AttendanceFrame.NAVY;
        if (getModel().isRollover() && !selected) bg = AttendanceFrame.PURPLE_HOVER;

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.dispose();

        super.paintComponent(g);
    }
}

class RoundedButton extends JButton {
    private final Color normal;
    private final Color hover;

    RoundedButton(String text, Color normal, Color hover) {
        super(text);
        this.normal = normal;
        this.hover = hover;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getModel().isRollover() ? hover : normal);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }
}

class RoundedPanel extends JPanel {
    private final Color color;
    private final int radius;

    RoundedPanel(LayoutManager lm, Color color, int radius) {
        super(lm);
        this.color = color;
        this.radius = radius;
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }
}

class Student {
    private final String id;
    private final String name;

    Student(String id, String name) {
        this.id = id;
        this.name = name;
    }

    String getId() { return id; }
    String getName() { return name; }
}

class AttendanceRecord {
    private final String date;
    private final String studentId;
    private final String studentName;
    private final String status;
    private final String comment;

    AttendanceRecord(String date, String studentId, String studentName, String status, String comment) {
        this.date = date;
        this.studentId = studentId;
        this.studentName = studentName;
        this.status = status;
        this.comment = comment;
    }

    String getDate() { return date; }
    String getStudentId() { return studentId; }
    String getStudentName() { return studentName; }
    String getStatus() { return status; }
    String getComment() { return comment; }
}

class CsvData {
    private final java.nio.file.Path dataDir = java.nio.file.Paths.get("data");
    private final java.nio.file.Path studentsFile = dataDir.resolve("students.csv");
    private final java.nio.file.Path attendanceFile = dataDir.resolve("attendance.csv");

    CsvData() { initialize(); }

    private void initialize() {
        try {
            java.nio.file.Files.createDirectories(dataDir);

            if (!java.nio.file.Files.exists(studentsFile)) {
                java.nio.file.Files.writeString(
                        studentsFile,
                        "student_id,name\n" +
                        "S101,Alice Tanaka\n" +
                        "S102,Bob Smith\n" +
                        "S103,Chen Wei\n" +
                        "S104,Dana Okafor\n" +
                        "S105,Elena Petrova\n",
                        java.nio.charset.StandardCharsets.UTF_8
                );
            }

            if (!java.nio.file.Files.exists(attendanceFile)) {
                java.nio.file.Files.writeString(attendanceFile, "", java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not initialize CSV files.", e);
        }
    }

    List<Student> loadStudents() {
        List<Student> result = new java.util.ArrayList<>();
        try {
            List<String> lines = java.nio.file.Files.readAllLines(studentsFile, java.nio.charset.StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).isBlank()) continue;
                String[] p = lines.get(i).split(",", 2);
                if (p.length == 2) result.add(new Student(p[0].trim(), p[1].trim()));
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not read students.csv.", e);
        }
        return result;
    }

    void addStudent(Student s) {
        try (java.io.BufferedWriter w = java.nio.file.Files.newBufferedWriter(
                studentsFile, java.nio.charset.StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND)) {
            w.write(csv(s.getId()) + "," + csv(s.getName()));
            w.newLine();
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not add student.", e);
        }
    }

    void deleteStudent(String id) {
        List<Student> list = loadStudents();
        try (java.io.BufferedWriter w = java.nio.file.Files.newBufferedWriter(
                studentsFile, java.nio.charset.StandardCharsets.UTF_8)) {
            w.write("student_id,name");
            w.newLine();
            for (Student s : list) {
                if (!s.getId().equals(id)) {
                    w.write(csv(s.getId()) + "," + csv(s.getName()));
                    w.newLine();
                }
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not update students.csv.", e);
        }
    }

    List<AttendanceRecord> loadAttendance() {
        List<AttendanceRecord> result = new java.util.ArrayList<>();
        try {
            if (!java.nio.file.Files.exists(attendanceFile)) return result;
            List<String> lines = java.nio.file.Files.readAllLines(attendanceFile, java.nio.charset.StandardCharsets.UTF_8);

            for (String line : lines) {
                if (line.isBlank() || line.toLowerCase().startsWith("date,")) continue;

                String[] p = line.split(",", 5);
                if (p.length >= 4) {
                    String comment = p.length == 5 ? p[4].trim() : "";
                    result.add(new AttendanceRecord(
                            p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(), comment
                    ));
                }
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not read attendance.csv.", e);
        }
        return result;
    }

    Map<String, String> getAttendanceForDate(String date) {
        Map<String, String> result = new LinkedHashMap<>();
        for (AttendanceRecord r : loadAttendance()) {
            if (r.getDate().equals(date)) result.put(r.getStudentId(), r.getStatus());
        }
        return result;
    }

    Map<String, String> getCommentsForDate(String date) {
        Map<String, String> result = new LinkedHashMap<>();
        for (AttendanceRecord r : loadAttendance()) {
            if (r.getDate().equals(date)) result.put(r.getStudentId(), r.getComment());
        }
        return result;
    }

    void saveAttendance(String date, List<Student> students,
                        Map<String, String> statuses, Map<String, String> comments) {
        List<AttendanceRecord> all = loadAttendance();
        List<AttendanceRecord> kept = new java.util.ArrayList<>();

        for (AttendanceRecord r : all) {
            if (!r.getDate().equals(date)) kept.add(r);
        }

        for (Student s : students) {
            String status = statuses.getOrDefault(s.getId(), "Not marked");
            String comment = comments.getOrDefault(s.getId(), "");

            // Do not save "Not marked" as attendance.
            if (!status.equals("Not marked")) {
                kept.add(new AttendanceRecord(date, s.getId(), s.getName(), status, comment));
            }
        }

        try (java.io.BufferedWriter w = java.nio.file.Files.newBufferedWriter(
                attendanceFile, java.nio.charset.StandardCharsets.UTF_8)) {
            w.write("date,student_id,name,status,comment");
            w.newLine();

            for (AttendanceRecord r : kept) {
                w.write(csv(r.getDate()) + "," +
                        csv(r.getStudentId()) + "," +
                        csv(r.getStudentName()) + "," +
                        csv(r.getStatus()) + "," +
                        csv(r.getComment()));
                w.newLine();
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not save attendance.csv.", e);
        }
    }

    int getAttendanceScore(String studentId) {
        int score = 100;
        for (AttendanceRecord r : loadAttendance()) {
            if (r.getStudentId().equals(studentId)) {
                if ("Absent".equals(r.getStatus())) score -= 5;
                else if ("Late".equals(r.getStatus())) score -= 2;
            }
        }
        return Math.max(0, score);
    }

    String exportDailyPdf(String date, List<Student> students) throws Exception {
        java.nio.file.Path outputDir = dataDir.resolve("reports");
        java.nio.file.Files.createDirectories(outputDir);
        java.nio.file.Path pdf = outputDir.resolve("attendance-" + date + ".pdf");

        com.lowagie.text.Document document = new com.lowagie.text.Document(
                com.lowagie.text.PageSize.A4, 36, 36, 36, 36
        );

        com.lowagie.text.pdf.PdfWriter.getInstance(
                document,
                java.nio.file.Files.newOutputStream(pdf)
        );

        document.open();

        com.lowagie.text.Font titleFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 20, com.lowagie.text.Font.BOLD
        );
        com.lowagie.text.Font normalFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 10
        );

        document.add(new com.lowagie.text.Paragraph("Daily Attendance Report", titleFont));
        document.add(new com.lowagie.text.Paragraph("Date: " + date, normalFont));
        document.add(new com.lowagie.text.Paragraph(" "));

        Map<String, String> statuses = getAttendanceForDate(date);
        Map<String, String> comments = getCommentsForDate(date);

        int present = 0, late = 0, absent = 0;

        com.lowagie.text.pdf.PdfPTable summary = new com.lowagie.text.pdf.PdfPTable(4);
        summary.setWidthPercentage(100);
        summary.addCell("Students");
        summary.addCell("Present");
        summary.addCell("Late");
        summary.addCell("Absent");

        for (Student s : students) {
            String status = statuses.getOrDefault(s.getId(), "Not marked");
            if ("Present".equals(status)) present++;
            else if ("Late".equals(status)) late++;
            else if ("Absent".equals(status)) absent++;
        }

        summary.addCell(String.valueOf(students.size()));
        summary.addCell(String.valueOf(present));
        summary.addCell(String.valueOf(late));
        summary.addCell(String.valueOf(absent));
        document.add(summary);
        document.add(new com.lowagie.text.Paragraph(" "));

        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.2f, 2.5f, 1.5f, 1.5f, 3.5f});

        table.addCell("ID");
        table.addCell("Name");
        table.addCell("Status");
        table.addCell("Score");
        table.addCell("Comment");

        for (Student s : students) {
            String status = statuses.getOrDefault(s.getId(), "Not marked");
            table.addCell(s.getId());
            table.addCell(s.getName());
            table.addCell(status);
            table.addCell(getAttendanceScore(s.getId()) + "%");
            table.addCell(comments.getOrDefault(s.getId(), ""));
        }

        document.add(table);
        document.close();

        return pdf.toAbsolutePath().toString();
    }

    private String csv(String value) {
        String v = value == null ? "" : value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) return "\"" + v + "\"";
        return v;
    }
}

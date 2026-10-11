import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.plaf.basic.BasicButtonUI;

// ============================================================
//  NexGen Care - Hospital Management System
//  Single-file version: all classes combined for easy compile/run.
//  Compile: javac NexGenCareHMS.java
//  Run:     java NexGenCareHMS
//  Login:   admin / admin
// ============================================================

public class NexGenCareHMS {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(LoginScreen::new);
    }
}

// ------------------------------------------------------------
// Person - base class
// ------------------------------------------------------------
class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String name;

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetails() {
        return "Person: " + name;
    }
}

// ------------------------------------------------------------
// Billable - interface
// ------------------------------------------------------------
interface Billable {
    int calculateBill();
}

// ------------------------------------------------------------
// Doctor - extends Person
// ------------------------------------------------------------
class Doctor extends Person implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<String> availableTimes;
    private ArrayList<String> bookedTimes;
    private String specialization;

    public Doctor(String name, ArrayList<String> availableTimes) {
        this(name, availableTimes, "General Medicine");
    }

    public Doctor(String name, ArrayList<String> availableTimes, String specialization) {
        super(name);
        this.availableTimes = new ArrayList<>(availableTimes);
        this.bookedTimes = new ArrayList<>();
        this.specialization = (specialization == null || specialization.isBlank())
                ? "General Medicine" : specialization;
    }

    public ArrayList<String> getAvailableTimes() { return availableTimes; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public boolean isAvailable(String time) {
        return !bookedTimes.contains(time);
    }

    public void bookSlot(String time) {
        if (!bookedTimes.contains(time)) bookedTimes.add(time);
    }

    public void freeSlot(String time) {
        bookedTimes.remove(time);
    }

    public int getBookedCount() {
        return bookedTimes.size();
    }

    /** Returns initials for avatar badges, e.g. "Dr. James Peiris" -> "JP" */
    public String getInitials() {
        String cleanName = name.replaceFirst("(?i)^dr\\.?\\s*", "").trim();
        String[] parts = cleanName.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) sb.append(Character.toUpperCase(p.charAt(0)));
            if (sb.length() >= 2) break;
        }
        return sb.length() == 0 ? "DR" : sb.toString();
    }

    @Override
    public String getDetails() {
        return "Doctor: " + name + " (" + specialization + ") | Slots Remaining: "
                + (availableTimes.size() - bookedTimes.size()) + "/" + availableTimes.size();
    }
}

// ------------------------------------------------------------
// Patient - extends Person, implements Billable
// (Treatment fields removed. Bill = fixed consultation fee.)
// ------------------------------------------------------------
class Patient extends Person implements Billable, Serializable {
    private static final long serialVersionUID = 1L;

    public static final int BASE_FEE = 1000;

    private String patientId;

    public Patient(String name) {
        this(name, "");
    }

    public Patient(String name, String patientId) {
        super(name);
        this.patientId = (patientId == null) ? "" : patientId;
    }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    @Override
    public int calculateBill() {
        return BASE_FEE;
    }

    @Override
    public String getDetails() {
        return "Patient: " + name + " | ID: " + patientId;
    }
}

// ------------------------------------------------------------
// Appointment - links Patient, Doctor, time, date
// ------------------------------------------------------------
class Appointment implements Serializable {
    private static final long serialVersionUID = 1L;

    private Patient patient;
    private Doctor doctor;
    private String time;
    private LocalDate date;

    public Appointment(Patient patient, Doctor doctor, String time) {
        this.patient = patient;
        this.doctor = doctor;
        this.time = time;
        this.date = LocalDate.now();
    }

    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public String getTime() { return time; }
    public LocalDate getDate() { return date; }

    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public void setTime(String time) { this.time = time; }
}

// ------------------------------------------------------------
// LoginScreen - entry point UI
// ------------------------------------------------------------
class LoginScreen extends JFrame implements ActionListener {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginBtn;

    public LoginScreen() {
        setTitle("Login - NexGen Care HMS");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(mainPanel);

        JLabel title = new JLabel("NexGen Care", JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(60, 130, 200));
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        JLabel userLabel = new JLabel("Username:");
        usernameField = new JTextField();
        JLabel passLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        formPanel.add(userLabel);
        formPanel.add(usernameField);
        formPanel.add(passLabel);
        formPanel.add(passwordField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(60, 130, 200));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.addActionListener(this);

        buttonPanel.add(loginBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void login() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();

        if (user.equals("admin") && pass.equals("admin")) {
            JOptionPane.showMessageDialog(this, "Login Successful!");
            dispose();
            new HospitalManagementSystem();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Credentials. Try again.");
            usernameField.setText("");
            passwordField.setText("");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginBtn) {
            login();
        }
    }
}

// ------------------------------------------------------------
// HospitalManagementSystem - main application window & logic
// ------------------------------------------------------------
class HospitalManagementSystem {
    private ArrayList<Doctor> doctors;
    private ArrayList<Appointment> appointments;

    private static final String DATA_FILE = "hms_data.ser";
    private static final Color BRAND = new Color(60, 130, 200);
    private static final Color BRAND_DARK = new Color(30, 90, 150);
    private static final Color AVAILABLE_COLOR = new Color(76, 175, 80);
    private static final Color BOOKED_COLOR = new Color(229, 83, 83);
    private static final Color BG_LIGHT = new Color(245, 247, 250);

    private JFrame frame;
    private JPanel slotsPanel;
    private JTextArea appointmentArea;
    private JTextField searchField;

    private JLabel statDoctorsValue;
    private JLabel statAppointmentsValue;
    private JLabel statRevenueValue;
    private JLabel statPatientsValue;

    ImageIcon icon = new ImageIcon("Logo.png");

    public HospitalManagementSystem() {
        doctors = new ArrayList<>();
        appointments = new ArrayList<>();

        if (!loadData()) {
            seedDefaultData();
        } else {
            removeAppointmentsWithoutPatientId();
        }

        showGUI();
    }

    /**
     * Removes only the appointments whose patient has a null/blank
     * Patient ID (records created before that field was added).
     */
    private void removeAppointmentsWithoutPatientId() {
        boolean removedAny = false;
        Iterator<Appointment> it = appointments.iterator();
        while (it.hasNext()) {
            Appointment a = it.next();
            String pid = a.getPatient().getPatientId();
            if (pid == null || pid.trim().isEmpty()) {
                a.getDoctor().freeSlot(a.getTime());
                it.remove();
                removedAny = true;
            }
        }
        if (removedAny) {
            saveData();
        }
    }

    private void seedDefaultData() {
        ArrayList<String> times1 = new ArrayList<>();
        times1.add(formatTime("9"));
        times1.add(formatTime("10"));
        times1.add(formatTime("11"));
        times1.add(formatTime("14"));
        doctors.add(new Doctor("Dr. James", times1, "Cardiology"));

        ArrayList<String> times2 = new ArrayList<>();
        times2.add(formatTime("15"));
        times2.add(formatTime("16"));
        times2.add(formatTime("17"));
        times2.add(formatTime("18"));
        doctors.add(new Doctor("Dr. Ben", times2, "General Medicine"));
    }

    // ==========================================
    // GUI Setup
    // ==========================================
    private void showGUI() {
        frame = new JFrame("NexGen Care HMS");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exitApplication();
            }
        });
        frame.setSize(1050, 760);
        frame.setLayout(new BorderLayout(0, 0));
        frame.getContentPane().setBackground(BG_LIGHT);
        frame.setIconImage(icon.getImage());
        frame.setJMenuBar(buildMenuBar());

        JPanel header = buildHeader();
        frame.add(header, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainContent.setBackground(BG_LIGHT);
        frame.add(mainContent, BorderLayout.CENTER);

        mainContent.add(buildDashboard(), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 10, 10));
        center.setBackground(BG_LIGHT);
        mainContent.add(center, BorderLayout.CENTER);

        slotsPanel = new JPanel();
        slotsPanel.setLayout(new BoxLayout(slotsPanel, BoxLayout.Y_AXIS));
        slotsPanel.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(slotsPanel);
        scroll.setBorder(BorderFactory.createTitledBorder("Doctors & Available Slots"));
        center.add(scroll);

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setBackground(BG_LIGHT);
        center.add(right);

        JPanel controls = new JPanel(new GridLayout(4, 1, 6, 6));
        controls.setBorder(BorderFactory.createTitledBorder("Controls"));
        controls.setBackground(Color.WHITE);

        JButton addDoctorBtn = styledButton("Add Doctor");
        addDoctorBtn.addActionListener(e -> addDoctorDialog());

        JButton removeDoctorBtn = styledButton("Remove Doctor");
        removeDoctorBtn.addActionListener(e -> removeDoctorDialog());

        JPanel searchPanel = new JPanel(new BorderLayout(6, 6));
        searchPanel.setBackground(Color.WHITE);
        searchField = new JTextField();
        searchField.setToolTipText("Search by patient name or Patient ID");
        JButton searchBtn = styledButton("Search Patient");
        searchBtn.addActionListener(e -> searchAppointments());

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        JButton saveBtn = styledButton("Save Data");
        saveBtn.addActionListener(e -> {
            saveData();
            JOptionPane.showMessageDialog(frame, "Data saved successfully.");
        });

        controls.add(addDoctorBtn);
        controls.add(removeDoctorBtn);
        controls.add(searchPanel);
        controls.add(saveBtn);
        right.add(controls, BorderLayout.NORTH);

        appointmentArea = new JTextArea();
        appointmentArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        appointmentArea.setEditable(false);
        JScrollPane appointmentScroll = new JScrollPane(appointmentArea);
        appointmentScroll.setBorder(BorderFactory.createTitledBorder("Appointments"));
        right.add(appointmentScroll, BorderLayout.CENTER);

        JLabel legend = new JLabel("Green = Available   |   Red = Booked", JLabel.CENTER);
        legend.setBorder(new EmptyBorder(6, 0, 6, 0));
        legend.setForeground(new Color(90, 90, 90));
        frame.add(legend, BorderLayout.SOUTH);

        addSlotPanels();
        updateAppointmentDisplay();
        updateDashboard();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BRAND);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("NexGen Care");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Hospital Management System", JLabel.RIGHT);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(225, 235, 245));
        header.add(subtitle, BorderLayout.EAST);

        return header;
    }

    private JPanel buildDashboard() {
        JPanel dashboard = new JPanel(new GridLayout(1, 4, 10, 0));
        dashboard.setBackground(BG_LIGHT);
        dashboard.setBorder(new EmptyBorder(0, 0, 10, 0));

        statDoctorsValue = new JLabel("0");
        statAppointmentsValue = new JLabel("0");
        statPatientsValue = new JLabel("0");
        statRevenueValue = new JLabel("0 LKR");

        dashboard.add(statCard("Active Doctors", statDoctorsValue, new Color(60, 130, 200)));
        dashboard.add(statCard("Today's Appointments", statAppointmentsValue, new Color(76, 175, 80)));
        dashboard.add(statCard("Total Patients", statPatientsValue, new Color(155, 89, 182)));
        dashboard.add(statCard("Today's Revenue", statRevenueValue, new Color(230, 126, 34)));

        return dashboard;
    }

    private JPanel statCard(String label, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, accent),
                new EmptyBorder(10, 14, 10, 14)));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(new Color(120, 120, 120));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(accent);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        return card;
    }

    private void updateDashboard() {
        statDoctorsValue.setText(String.valueOf(doctors.size()));

        LocalDate today = LocalDate.now();
        long todaysCount = appointments.stream().filter(a -> today.equals(a.getDate())).count();
        statAppointmentsValue.setText(String.valueOf(todaysCount));

        statPatientsValue.setText(String.valueOf(appointments.size()));

        int revenue = appointments.stream()
                .filter(a -> today.equals(a.getDate()))
                .mapToInt(a -> a.getPatient().calculateBill())
                .sum();
        statRevenueValue.setText(new DecimalFormat("#,###").format(revenue) + " LKR");
    }

    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(240, 244, 248));
        return btn;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save Data");
        saveItem.addActionListener(e -> {
            saveData();
            JOptionPane.showMessageDialog(frame, "Data saved successfully.");
        });
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> exitApplication());
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "NexGen Care Hospital Management System\nVersion 2.0\n\n" +
                        "A Java Swing desktop application for managing doctors,\n" +
                        "appointments, and billing.",
                "About NexGen Care", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private void exitApplication() {
        int confirm = JOptionPane.showConfirmDialog(frame,
                "Save data before exiting?", "Exit NexGen Care",
                JOptionPane.YES_NO_CANCEL_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            saveData();
            System.exit(0);
        } else if (confirm == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
    }

    // ==========================================
    // Slot Panels
    // ==========================================
    private void addSlotPanels() {
        slotsPanel.removeAll();
        for (Doctor doc : doctors) {
            JPanel doctorPanel = new JPanel(new BorderLayout(10, 0));
            doctorPanel.setBorder(BorderFactory.createTitledBorder(
                    doc.getName() + "  —  " + doc.getSpecialization()));
            doctorPanel.setBackground(Color.WHITE);

            JLabel avatar = new JLabel(doc.getInitials(), JLabel.CENTER);
            avatar.setOpaque(true);
            avatar.setBackground(BRAND_DARK);
            avatar.setForeground(Color.WHITE);
            avatar.setFont(new Font("SansSerif", Font.BOLD, 14));
            avatar.setPreferredSize(new Dimension(40, 40));
            JPanel avatarWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
            avatarWrap.setBackground(Color.WHITE);
            avatarWrap.add(avatar);
            doctorPanel.add(avatarWrap, BorderLayout.WEST);

            JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
            timePanel.setBackground(Color.WHITE);

            for (String time : doc.getAvailableTimes()) {
                JButton btn = new JButton(time);
                btn.setUI(new BasicButtonUI());
                btn.setOpaque(true);
                btn.setContentAreaFilled(true);
                btn.setBorderPainted(false);
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("SansSerif", Font.BOLD, 12));
                btn.setFocusPainted(false);
                btn.setPreferredSize(new Dimension(92, 32));
                btn.setBackground(doc.isAvailable(time) ? AVAILABLE_COLOR : BOOKED_COLOR);
                btn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (doc.isAvailable(time)) scheduleAppointment(doc, time);
                        else {
                            Appointment app = findAppointment(doc, time);
                            if (app != null) {
                                manageAppointment(app);
                            }
                        }
                    }
                });
                timePanel.add(btn);
            }
            doctorPanel.add(timePanel, BorderLayout.CENTER);
            slotsPanel.add(doctorPanel);
        }
        slotsPanel.revalidate();
        slotsPanel.repaint();
    }

    // --------------------------------------------------------
    // New appointment: asks for Patient Name + Patient ID only.
    // (Treatment selection removed.)
    // --------------------------------------------------------
    private void scheduleAppointment(Doctor doctor, String time) {
        JTextField nameField = new JTextField();
        JTextField idField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(2, 2, 6, 6));
        panel.add(new JLabel("Patient Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Patient ID:"));
        panel.add(idField);

        int nameResult = JOptionPane.showConfirmDialog(frame, panel,
                "New Appointment - " + doctor.getName() + " at " + time,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (nameResult != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        String patientId = idField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Patient name cannot be empty.");
            return;
        }
        if (patientId.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Patient ID cannot be empty.");
            return;
        }

        Patient patient = new Patient(name, patientId);

        Appointment app = new Appointment(patient, doctor, time);
        appointments.add(app);
        doctor.bookSlot(time);

        addSlotPanels();
        updateAppointmentDisplay();
        updateDashboard();
    }

    private void manageAppointment(Appointment app) {
        String[] options = {"Reschedule", "Cancel", "Generate Bill", "Close"};
        int choice = JOptionPane.showOptionDialog(frame, getAppointmentDetails(app), "Manage Appointment",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        switch (choice) {
            case 0:
                rescheduleAppointment(app);
                break;
            case 1:
                cancelAppointment(app);
                break;
            case 2:
                generateBill(app);
                break;
        }
    }

    private void rescheduleAppointment(Appointment app) {
        JComboBox<String> doctorBox = new JComboBox<>(getDoctorNames());
        JComboBox<String> timeBox = new JComboBox<>();
        doctorBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeBox.removeAllItems();
                Doctor selected = doctors.get(doctorBox.getSelectedIndex());
                for (String time : selected.getAvailableTimes()) {
                    if (selected.isAvailable(time) || (selected == app.getDoctor() && time.equals(app.getTime()))) {
                        timeBox.addItem(time);
                    }
                }
            }
        });

        JPanel panel = new JPanel(new GridLayout(2, 2, 6, 6));
        panel.add(new JLabel("Doctor:"));
        panel.add(doctorBox);
        panel.add(new JLabel("Time:"));
        panel.add(timeBox);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Reschedule Appointment", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Doctor newDoc = doctors.get(doctorBox.getSelectedIndex());
            String newTime = (String) timeBox.getSelectedItem();
            if (newTime == null) {
                return;
            }

            app.getDoctor().freeSlot(app.getTime());
            newDoc.bookSlot(newTime);
            app.setDoctor(newDoc);
            app.setTime(newTime);

            addSlotPanels();
            updateAppointmentDisplay();
            updateDashboard();
        }
    }

    private void cancelAppointment(Appointment app) {
        int confirm = JOptionPane.showConfirmDialog(frame, "Cancel appointment for " + app.getPatient().getName() + "?",
                "Confirm Cancel", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            app.getDoctor().freeSlot(app.getTime());
            appointments.remove(app);
            addSlotPanels();
            updateAppointmentDisplay();
            updateDashboard();
        }
    }

    private void generateBill(Appointment app) {
        Billable billable = app.getPatient();
        int total = billable.calculateBill();
        String receipt = buildReceiptText(app, total);

        JTextArea area = new JTextArea(receipt);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));

        Object[] options = {"Export as .txt", "Close"};
        int choice = JOptionPane.showOptionDialog(frame, new JScrollPane(area), "Bill / Receipt",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);

        if (choice == 0) {
            exportReceipt(app, receipt);
        }
    }

    private String buildReceiptText(Appointment app, int total) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("           NEXGEN CARE HOSPITAL          \n");
        sb.append("               PAYMENT RECEIPT            \n");
        sb.append("========================================\n\n");
        sb.append("Date        : ").append(app.getDate().format(fmt)).append("\n");
        sb.append("Patient     : ").append(app.getPatient().getName()).append("\n");
        sb.append("Patient ID  : ").append(app.getPatient().getPatientId()).append("\n");
        sb.append("Doctor      : ").append(app.getDoctor().getName())
                .append(" (").append(app.getDoctor().getSpecialization()).append(")\n");
        sb.append("Time        : ").append(app.getTime()).append("\n");
        sb.append("----------------------------------------\n");
        sb.append(String.format("Base Fee    : %,d LKR%n", Patient.BASE_FEE));
        sb.append("----------------------------------------\n");
        sb.append(String.format("TOTAL       : %,d LKR%n", total));
        sb.append("========================================\n");
        sb.append("        Thank you for choosing us!        \n");
        sb.append("========================================\n");
        return sb.toString();
    }

    private void exportReceipt(Appointment app, String receipt) {
        String safeName = app.getPatient().getName().replaceAll("[^a-zA-Z0-9]", "_");
        String fileName = "Receipt_" + safeName + "_" + System.currentTimeMillis() + ".txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            pw.print(receipt);
            JOptionPane.showMessageDialog(frame, "Receipt exported as:\n" + fileName);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Failed to export receipt: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --------------------------------------------------------
    // Search by patient NAME or PATIENT ID (partial, case-insensitive)
    // with a "Delete Selected" button in the results.
    // --------------------------------------------------------
    private void searchAppointments() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Enter a patient name or Patient ID to search.");
            return;
        }

        ArrayList<Appointment> matches = new ArrayList<>();
        for (Appointment a : appointments) {
            String nameLower = a.getPatient().getName().toLowerCase();
            String idLower = a.getPatient().getPatientId() == null ? "" : a.getPatient().getPatientId().toLowerCase();
            if (nameLower.contains(query) || idLower.contains(query)) {
                matches.add(a);
            }
        }

        if (matches.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No matching records found for \"" + query + "\".");
            return;
        }

        showSearchResultsDialog(matches, query);
    }

    private void showSearchResultsDialog(ArrayList<Appointment> matches, String query) {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Appointment a : matches) {
            listModel.addElement(searchResultLine(a));
        }

        JList<String> resultsList = new JList<>(listModel);
        resultsList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        JScrollPane resultsScroll = new JScrollPane(resultsList);
        resultsScroll.setPreferredSize(new Dimension(560, 220));

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(new JLabel("Results for \"" + query + "\" (" + matches.size() + " found):"), BorderLayout.NORTH);
        panel.add(resultsScroll, BorderLayout.CENTER);

        JButton deleteBtn = new JButton("Delete Selected Record");
        deleteBtn.setForeground(new Color(180, 40, 40));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.add(deleteBtn);
        panel.add(btnRow, BorderLayout.SOUTH);

        deleteBtn.addActionListener(e -> {
            int idx = resultsList.getSelectedIndex();
            if (idx == -1) {
                JOptionPane.showMessageDialog(frame, "Select a record from the list first.");
                return;
            }
            Appointment toDelete = matches.get(idx);
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Permanently delete this patient's record?\n\n" + getAppointmentDetails(toDelete),
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                toDelete.getDoctor().freeSlot(toDelete.getTime());
                appointments.remove(toDelete);
                matches.remove(idx);
                listModel.remove(idx);

                addSlotPanels();
                updateAppointmentDisplay();
                updateDashboard();

                JOptionPane.showMessageDialog(frame, "Record deleted.");
            }
        });

        JOptionPane.showMessageDialog(frame, panel, "Search Results", JOptionPane.PLAIN_MESSAGE);
    }

    private String searchResultLine(Appointment a) {
        return String.format("%-18s | ID: %-10s | %-15s | %s",
                a.getPatient().getName(),
                a.getPatient().getPatientId(),
                a.getDoctor().getName(),
                a.getTime());
    }

    private static final String[] SPECIALIZATIONS = {
            "General Medicine", "Cardiology", "Neurology", "Pediatrics",
            "Orthopedics", "Dermatology", "ENT (Otolaryngology)", "Ophthalmology",
            "Gynecology & Obstetrics", "Psychiatry", "Dentistry", "Urology",
            "Oncology", "Radiology", "Endocrinology", "Gastroenterology",
            "Nephrology", "Pulmonology", "General Surgery", "Other (type below)"
    };

    private void addDoctorDialog() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(420, 420));

        JTextField nameField = new JTextField();
        JComboBox<String> specBox = new JComboBox<>(SPECIALIZATIONS);
        JTextField customSpecField = new JTextField();
        customSpecField.setEnabled(false);

        specBox.addActionListener(e ->
                customSpecField.setEnabled(specBox.getSelectedItem() == SPECIALIZATIONS[SPECIALIZATIONS.length - 1]));

        JPanel formGrid = new JPanel(new GridLayout(3, 2, 6, 6));
        formGrid.add(new JLabel("Doctor Name:"));
        formGrid.add(nameField);
        formGrid.add(new JLabel("Specialization:"));
        formGrid.add(specBox);
        formGrid.add(new JLabel("Custom (if \"Other\"):"));
        formGrid.add(customSpecField);
        panel.add(formGrid);

        panel.add(Box.createVerticalStrut(12));

        JLabel timesLabel = new JLabel("Available Times:");
        timesLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        timesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(timesLabel);
        panel.add(Box.createVerticalStrut(4));

        String[] hours12 = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        String[] minutes = {"00", "15", "30", "45"};
        String[] ampm = {"AM", "PM"};

        JComboBox<String> hourBox = new JComboBox<>(hours12);
        JComboBox<String> minuteBox = new JComboBox<>(minutes);
        JComboBox<String> ampmBox = new JComboBox<>(ampm);
        JButton addTimeBtn = new JButton("+ Add");

        JPanel pickerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        pickerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        pickerRow.add(hourBox);
        pickerRow.add(new JLabel(":"));
        pickerRow.add(minuteBox);
        pickerRow.add(ampmBox);
        pickerRow.add(addTimeBtn);
        panel.add(pickerRow);

        DefaultListModel<String> timesModel = new DefaultListModel<>();
        JList<String> timesList = new JList<>(timesModel);
        timesList.setVisibleRowCount(5);
        JScrollPane timesScroll = new JScrollPane(timesList);
        timesScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        timesScroll.setPreferredSize(new Dimension(380, 110));
        timesScroll.setBorder(BorderFactory.createTitledBorder("Slots for this doctor"));
        panel.add(timesScroll);

        JButton removeTimeBtn = new JButton("Remove Selected");
        removeTimeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(Box.createVerticalStrut(4));
        panel.add(removeTimeBtn);

        addTimeBtn.addActionListener(e -> {
            String label = String.format("%02d:%s %s",
                    Integer.parseInt((String) hourBox.getSelectedItem()),
                    minuteBox.getSelectedItem(),
                    ampmBox.getSelectedItem());
            if (!timesModel.contains(label)) {
                timesModel.addElement(label);
            }
        });

        removeTimeBtn.addActionListener(e -> {
            int idx = timesList.getSelectedIndex();
            if (idx != -1) {
                timesModel.remove(idx);
            }
        });

        int result = JOptionPane.showConfirmDialog(frame, panel, "Add Doctor", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String selectedSpec = (String) specBox.getSelectedItem();
            String spec = selectedSpec.equals(SPECIALIZATIONS[SPECIALIZATIONS.length - 1])
                    ? customSpecField.getText().trim()
                    : selectedSpec;

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Enter the doctor's name.");
                return;
            }
            if (timesModel.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Add at least one available time (pick Hour/Minute/AM-PM, then click + Add).");
                return;
            }
            if (!name.toLowerCase().startsWith("dr.")) {
                name = "Dr. " + name;
            }

            ArrayList<String> tlist = new ArrayList<>();
            for (int i = 0; i < timesModel.size(); i++) {
                tlist.add(timesModel.get(i));
            }

            doctors.add(new Doctor(name, tlist, spec.isEmpty() ? "General Medicine" : spec));
            addSlotPanels();
            updateDashboard();
        }
    }

    private void removeDoctorDialog() {
        if (doctors.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No doctors available.");
            return;
        }
        JComboBox<String> box = new JComboBox<>(getDoctorNames());
        int result = JOptionPane.showConfirmDialog(frame, box, "Deactivate Doctor", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Doctor doc = doctors.get(box.getSelectedIndex());
            JOptionPane.showMessageDialog(frame, doc.getName() + " has been deactivated (past records remain).");
            doctors.remove(doc);
            addSlotPanels();
            updateAppointmentDisplay();
            updateDashboard();
        }
    }

    private Appointment findAppointment(Doctor d, String time) {
        for (Appointment a : appointments) {
            if (a.getDoctor() == d && a.getTime().equals(time)) {
                return a;
            }
        }
        return null;
    }

    // --------------------------------------------------------
    // Appointments table: No / Patient / Patient ID / Doctor / Time
    // (Treatment column removed.)
    // --------------------------------------------------------
    private void updateAppointmentDisplay() {
        String display = String.format("%-4s %-20s %-12s %-20s %-12s%n", "No", "Patient", "Patient ID", "Doctor", "Time");
        display += "--------------------------------------------------------------------\n";
        int i = 1;
        for (Appointment a : appointments) {
            display += String.format("%-4d %-20s %-12s %-20s %-12s%n",
                    i++, a.getPatient().getName(), a.getPatient().getPatientId(), a.getDoctor().getName(),
                    a.getTime());
        }
        appointmentArea.setText(display);
    }

    private String[] getDoctorNames() {
        String[] names = new String[doctors.size()];
        for (int i = 0; i < doctors.size(); i++) {
            names[i] = doctors.get(i).getName();
        }
        return names;
    }

    private String getAppointmentDetails(Appointment a) {
        return a.getPatient().getDetails() + "\n" + a.getDoctor().getDetails() + "\nTime: " + a.getTime();
    }

    private String formatTime(String time) {
        try {
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = (parts.length > 1) ? Integer.parseInt(parts[1]) : 0;
            String period = (hour >= 12) ? "PM" : "AM";
            int displayHour = (hour == 0) ? 12 : (hour > 12 ? hour - 12 : hour);
            DecimalFormat df = new DecimalFormat("00");
            return df.format(displayHour) + ":" + df.format(minute) + " " + period;
        } catch (Exception e) {
            return time;
        }
    }

    // ==========================================
    // Data Persistence
    // ==========================================
    @SuppressWarnings("unchecked")
    private boolean loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return false;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            doctors = (ArrayList<Doctor>) ois.readObject();
            appointments = (ArrayList<Appointment>) ois.readObject();
            return true;
        } catch (Exception e) {
            System.err.println("Could not load saved data: " + e.getMessage());
            doctors = new ArrayList<>();
            appointments = new ArrayList<>();
            return false;
        }
    }

    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(doctors);
            oos.writeObject(appointments);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Failed to save data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

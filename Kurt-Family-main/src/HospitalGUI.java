import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;


public class HospitalGUI extends JFrame implements LogListener {

    private final HospitalSystem system;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    private final DateTimeFormatter releaseDtf = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");

    private DefaultTableCellRenderer createTooltipRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JComponent) {
                    String txt = value == null ? "" : value.toString();
                    ((JComponent) c).setToolTipText(txt);
                }
                return c;
            }
        };
    }

    private void startSimulation() {
        if (simRunning) return;
        simRunning = true;
        simToggleBtn.setText("Stop Simulation");
        simTimer.setDelay(simIntervalMs);
        simTimer.start();
        refreshStats();
    }

    private void stopSimulation() {
        if (!simRunning) return;
        simRunning = false;
        simToggleBtn.setText("Start Simulation");
        simTimer.stop();
        refreshStats();
    }

    private void simTick() {
        if (simRand.nextDouble() < 0.4) {
            Patient p = createRandomPatient();
            system.addPatientToER(p);
        }

        PriorityQueue<Patient> tri = system.getTriageQueue().getQueue();
        if (!tri.isEmpty() && simRand.nextDouble() < 0.6) {
            Patient next = tri.peek();
            List<String> depts = new ArrayList<>(system.getDepartments().keySet());
            if (!depts.isEmpty()) {
                String dept = depts.get(simRand.nextInt(depts.size()));
                system.movePatientToDepartmentById(next.getId(), dept);
            }
        }

        system.autoFillAllDepartments();

        for (String deptName : system.getDepartments().keySet()) {
            Department dept = system.getDepartments().get(deptName);
            List<Department.OngoingEntry> ongoing = dept.getOngoingSnapshot();
            for (Department.OngoingEntry entry : ongoing) {
                double base = 0.25;
                String sev = entry.getPatient().getSeverity().name();
                if ("CRITICAL".equals(sev)) base = 0.04;
                else if ("SEVERE".equals(sev)) base = 0.08;
                else if ("MODERATE".equals(sev)) base = 0.18;
                else if ("MILD".equals(sev)) base = 0.35;
                else base = 0.45;
                if (simRand.nextDouble() < base) {
                    system.releaseOngoingPatient(deptName, entry.getPatient().getId());
                } else if (simRand.nextDouble() < 0.03) {
                    int room = 1 + simRand.nextInt(101);
                    int days = 1 + simRand.nextInt(10);
                    if (!system.isRoomOccupied(room)) {
                        system.confineOngoingPatient(deptName, entry.getPatient().getId(), room, days);
                    }
                }
            }
        }

        for (String deptName : system.getDepartments().keySet()) {
            Department dept = system.getDepartments().get(deptName);
            List<Patient> confined = dept.getConfinedSnapshot();
            for (Patient p : confined) {
                if (p.getReleaseTime() != null && p.getReleaseTime().isBefore(LocalDateTime.now())) {
                    system.releaseConfinedPatient(deptName, p.getId());
                } else if (simRand.nextDouble() < 0.02) {
                    system.releaseConfinedPatient(deptName, p.getId());
                }
            }
        }

        refreshAll();
    }

    private static final Color PRIMARY_COLOR = new Color(10, 90, 140); 
    private static final Color SECONDARY_COLOR = new Color(235, 245, 250); 
    private static final Color ACCENT_COLOR = new Color(34, 139, 34); 
    private static final Color BACKGROUND_COLOR = new Color(245, 250, 252); 
    private static final Color BORDER_COLOR = new Color(200, 220, 230); 
    private static final Color TEXT_COLOR = new Color(50, 50, 50); 
    private static final Color SUBTLE_TEXT_COLOR = new Color(80, 80, 90); 

    private JTable erTable;
    private DefaultTableModel erModel;

    private final Map<String, JTable> waitingTables = new LinkedHashMap<>();
    private final Map<String, JTable> ongoingTables = new LinkedHashMap<>();
    private final Map<String, DefaultListModel<String>> doctorListModels = new LinkedHashMap<>();
    private final Map<String, JList<String>> doctorJLists = new LinkedHashMap<>();
    private final Map<Integer, String> doctorDisplayNames = new HashMap<>();
    private final Set<String> usedDoctorNames = new HashSet<>();
    private final Random nameRand = new Random();
    private static final String[] DOCTOR_FIRST_NAMES = {"Alex","Sam","Jordan","Taylor","Morgan","Casey","Riley","Jamie","Avery","Cameron","Drew","Quinn","Parker","Reese","Kendall","Rowan","Logan","Elliot","Harper","Blake"};
    private static final String[] DOCTOR_LAST_NAMES = {"Reyes","Santos","Garcia","Lopez","Silva","Morales","Cruz","Torres","Nguyen","Patel","Kim","Singh","Ortiz","Gonzalez","Ng","Chen","Khan","Park","Ali","Ramos"};

    private String generateDoctorName() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String fn = DOCTOR_FIRST_NAMES[nameRand.nextInt(DOCTOR_FIRST_NAMES.length)];
            String ln = DOCTOR_LAST_NAMES[nameRand.nextInt(DOCTOR_LAST_NAMES.length)];
            String name = "Dr. " + fn + " " + ln;
            if (!usedDoctorNames.contains(name)) {
                usedDoctorNames.add(name);
                return name;
            }
        }
        String fallback = "Dr." + (1000 + nameRand.nextInt(9000));
        usedDoctorNames.add(fallback);
        return fallback;
    }

    private JTable confinedTable;
    private DefaultTableModel confinedModel;

    private DefaultTableModel recordsModel;
    private JTextField recordsSearchField;

    private JPanel statsPanel;
    private final Map<String, String> deptDescriptions = new LinkedHashMap<>();

    private JLabel timeLabel;
    private final ZoneId philippineZone = ZoneId.of("Asia/Manila");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    private Timer simTimer;
    private int simIntervalMs = 1000;
    private boolean simRunning = false;
    private JButton simToggleBtn;
    private JComboBox<String> simSpeedCombo;
    private final Random simRand = new Random();

    private static final String[] PATIENT_NAMES = {
        "Grey Namor", "Sarah Connor", "Emily Davis", "Laura Martin", "Jessica Wilson",
        "Amanda Anderson", "Ashley Moore", "Sarah Lee", "Jane Doe", "Anna Thompson",
        "Olivia Brown", "Sophia Taylor", "Emma Harris", "Isabella Clark", "Mia Lewis",
        "Michael Johnson", "Joshua Taylor", "Daniel Thomas", "David Harris", "James Thompson",
        "Christopher Brown", "Matthew Miller", "John Smith", "Robert Wilson", "William Moore",
        "Joseph Anderson", "Benjamin Martin", "Samuel Lee", "Alexander Harris", "Ryan Clark",
        "Andrew Lewis", "Nathan Walker", "Ethan Robinson", "Jack Hall", "Liam Young",
        "Noah King", "Lucas Wright", "Owen Scott", "Caleb Green", "Gabriel Adams",
        "Zoe Parker", "Chloe Nelson", "Lily Carter", "Ella Mitchell", "Grace Morgan",
        "Hannah Cooper", "Madison Richardson", "Avery Reed", "Aria Bailey", "Scarlett Bell",
        "Victoria Brooks", "Samantha Foster", "Megan Hughes", "Natalie James", "Leah Kelly",
        "Julia Price", "Caroline Sanders", "Audrey Simmons", "Stella Ward", "Lillian Watson",
        "Hailey Brooks", "Addison Russell", "Kayla Bennett", "Savannah Coleman", "Brooke Perry",
        "Mackenzie Powell", "Sydney Long", "Faith Patterson", "Melanie Hughes", "Clara Jenkins",
        "Evelyn Ross", "Alice Coleman", "Hazel Butler", "Violet Simmons", "Nora Foster",
        "Ruby Griffin", "Ella Dawson", "Ivy Harper", "Lucy Grant", "Paisley Chapman",
        "Julian Ross", "Leo Ford", "Dominic Hunter", "Sebastian Hayes", "Adrian Palmer",
        "Eli Simmons", "Isaac Morrison", "Mason Reid", "Carter Stevens", "Wyatt Freeman",
        "Jaxon Knight", "Grayson Lane", "Hudson Ellis", "Easton Warren", "Miles Parker",
        "Lincoln Hughes", "Gavin Foster", "Colton Bailey", "Asher Sullivan", "Blake Dawson"
    };

    private static final String SAMPLE_CASES_CSV =
        "Chest Pain, Shortness of Breath, High Fever, Traumatic Fracture, Severe Headache, " +
        "Wheezing/Asthma, Abdominal Pain, Vomiting, GI Bleed, Active Bleeding, Altered Mental Status, " +
        "Stroke Symptoms, High Blood Sugar (DKA), Pregnancy Pain, Allergic Reaction, Wound Infection, " +
        "Palpitations, Syncope/Fainting, Pediatric Seizure, Neonatal Jaundice, Urinary Retention, " +
        "Electrolyte Abnormality, Concussion, Burn, Obstetric Bleed, Psychiatric Crisis, Drug Overdose, " +
        "Dehydration, Respiratory Distress, Back Pain, Ear Pain, Eye Complaint, Cellulitis, " +
        "Hypertensive Emergency, Hypotension, Musculoskeletal Pain, Minor Laceration, Foreign Body, " +
        "Tooth/Oral Pain, Respiratory Arrest, Rash, UTI, Kidney Stone, Dental Abscess, Sepsis Concern, " +
        "Fracture - Arm, Fracture - Leg, Fracture - Hip, Eye Trauma, Shortness on Exertion, Wound Care, " +
        "Suture Needed, Nosebleed, Minor Burn, Heat Exhaustion, Hypoglycemia, Abnormal Bleeding, " +
        "Immunization Reaction, Skin Rash, Dizziness, Leg Swelling, Possible DVT, Pulmonary Embolism, " +
        "Chest Wall Strain, Ankle Sprain, Upper Respiratory Infection, Gastroenteritis, Ear Infection, " +
        "Sore Throat, Bronchitis, Worsening COPD, Pediatric Fever - Mild, Adult Flu";

    private static final String[] SAMPLE_CASES_ARRAY = Arrays.stream(SAMPLE_CASES_CSV.split(","))
            .map(String::trim)
            .toArray(String[]::new);
            public HospitalGUI() {
        super("Hospital Management System - Final (Priority + Rooms)");
        system = new HospitalSystem();
        system.setLogListener(this);

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; }
            }
        } catch (Exception ignored) {}

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1320, 860));
        setLayout(new BorderLayout(8,8));
        getContentPane().setBackground(BACKGROUND_COLOR);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(SECONDARY_COLOR);
        tabs.setForeground(TEXT_COLOR);
        tabs.setFont(tabs.getFont().deriveFont(Font.PLAIN, 11f));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        UIManager.put("TabbedPane.tabInsets", new Insets(4,8,4,8));

        JPanel topControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        topControls.setOpaque(false);
        simToggleBtn = new JButton("Start Simulation");
        simSpeedCombo = new JComboBox<>(new String[]{"1x","2x","5x","10x"});
        simSpeedCombo.setSelectedIndex(0);
        styleButton(simToggleBtn);
        topControls.add(new JLabel("Simulation:"));
        topControls.add(simToggleBtn);
        topControls.add(new JLabel("Speed:"));
        topControls.add(simSpeedCombo);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(topControls, BorderLayout.NORTH);
        centerWrapper.add(tabs, BorderLayout.CENTER);

        tabs.add("ER", createERPanel());
        tabs.add("Departments", createDepartmentsPanel());
        tabs.add("Confined", createConfinedPanel());
        tabs.add("Records", createRecordsPanel());
        tabs.add("Reports", createReportsPanel());

        add(centerWrapper, BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        statusBar.setBackground(SECONDARY_COLOR);
        statusBar.setBorder(new EmptyBorder(5, 10, 5, 10));
        timeLabel = new JLabel("Loading time...");
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        timeLabel.setForeground(PRIMARY_COLOR);
        statusBar.add(timeLabel);
        add(statusBar, BorderLayout.SOUTH);
        startPhilippineTimeUpdater();

        simTimer = new Timer(simIntervalMs, e -> simTick());
        simToggleBtn.addActionListener(e -> {
            if (!simRunning) {
                startSimulation();
            } else {
                stopSimulation();
            }
        });
        simSpeedCombo.addActionListener(e -> {
            String s = (String) simSpeedCombo.getSelectedItem();
            int mult = 1;
            if ("2x".equals(s)) mult = 2;
            else if ("5x".equals(s)) mult = 5;
            else if ("10x".equals(s)) mult = 10;
            simIntervalMs = Math.max(100, 1000 / mult);
            if (simRunning) {
                simTimer.setDelay(simIntervalMs);
            }
        });

        pack();
        setLocationRelativeTo(null);
        refreshAll();
        setVisible(true);
    }

    private void updatePhilippineTime() {
        ZonedDateTime now = ZonedDateTime.now(philippineZone);
        timeLabel.setText("PH Time: " + now.format(timeFormatter));
    }

    private void startPhilippineTimeUpdater() {
        Timer timer = new Timer(1000, e -> updatePhilippineTime());
        timer.setInitialDelay(0);
        timer.start();
    }

    private JPanel createERPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setBackground(BACKGROUND_COLOR);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setOpaque(false);

        JButton addRandom = new JButton("Newly Arrived Patient");
        JButton addManual = new JButton("Manually Add Patient");
        JButton moveToDept = new JButton("Move Patient to Dept");
        JButton assignSev = new JButton("Manual Assign Severity");

        styleButton(addRandom);
        styleButton(addManual);
        styleButton(moveToDept);
        styleButton(assignSev);

        top.add(addRandom);
        top.add(addManual);
        top.add(moveToDept);
        top.add(assignSev);
        p.add(top, BorderLayout.NORTH);

        erModel = new DefaultTableModel(new Object[]{"ID", "Name", "Age", "Case", "Severity", "Arrival"}, 0){
            public boolean isCellEditable(int r, int c) { return false; }
        };
        erTable = new JTable(erModel);
        erTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        erTable.getColumnModel().getColumn(4).setCellRenderer(new SeverityCellRenderer());
        erTable.getTableHeader().setBackground(SECONDARY_COLOR);
        erTable.getTableHeader().setForeground(PRIMARY_COLOR);
        erTable.setFillsViewportHeight(true);
        

        JScrollPane erScroll = new JScrollPane(erTable);
        erScroll.setWheelScrollingEnabled(true);
        erScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        p.add(erScroll, BorderLayout.CENTER);

        addRandom.addActionListener(e -> { system.addPatientToER(createRandomPatient()); refreshAll(); });

        addManual.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = JOptionPane.showInputDialog(HospitalGUI.this, "Name:");
                    if (name == null) return;
                    String ageStr = JOptionPane.showInputDialog(HospitalGUI.this, "Age:");
                    if (ageStr == null) return;
                    int age = Integer.parseInt(ageStr.trim());
                    String cs = JOptionPane.showInputDialog(HospitalGUI.this, "Case:");
                    if (cs == null) return;
                    Patient.Severity sev = (Patient.Severity) JOptionPane.showInputDialog(HospitalGUI.this, "Severity:", "Severity", JOptionPane.PLAIN_MESSAGE, null, Patient.Severity.values(), Patient.Severity.MODERATE);
                    if (sev == null) return;
                    system.addPatientToER(new Patient(name, age, cs, sev, LocalDateTime.now()));
                    refreshAll();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(HospitalGUI.this, "Invalid input: " + ex.getMessage());
                }
            }
        });

        moveToDept.addActionListener(e -> {
            int sel = erTable.getSelectedRow();
            if (sel < 0) { JOptionPane.showMessageDialog(this, "Select a patient in ER first."); return; }
            int id = (int) erModel.getValueAt(sel, 0);
            Object[] opts = system.getDepartments().keySet().toArray();
            String dept = (String) JOptionPane.showInputDialog(this, "Choose department:", "Move to Department", JOptionPane.PLAIN_MESSAGE, null, opts, opts.length>0?opts[0]:null);
            if (dept == null) return;
            boolean ok = system.movePatientToDepartmentById(id, dept);
            if (!ok) JOptionPane.showMessageDialog(this, "Move failed (patient may have changed).");
            refreshAll();
        });

        assignSev.addActionListener(e -> {
            int sel = erTable.getSelectedRow();
            if (sel < 0) { JOptionPane.showMessageDialog(this, "Select a patient in ER first."); return; }
            int id = (int) erModel.getValueAt(sel, 0);
            Patient patient = system.findPatientInTriageById(id);
            if (patient == null) { JOptionPane.showMessageDialog(this, "Patient not found (maybe moved)"); return; }
            Patient.Severity newSev = (Patient.Severity) JOptionPane.showInputDialog(this, "Severity:", "Assign Severity", JOptionPane.PLAIN_MESSAGE, null, Patient.Severity.values(), patient.getSeverity());
            if (newSev != null) {
                patient.setSeverity(newSev);
                system.getTriageQueue().getQueue().remove(patient);
                system.getTriageQueue().addPatient(patient);
                refreshAll();
            }
        });

        return p;
    }

    private JPanel createReportsPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setBackground(BACKGROUND_COLOR);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.setOpaque(false);

        JComboBox<String> reportType = new JComboBox<>(new String[]{"Department Summary", "Patients (ER)", "Patients (Released)", "Patients (All)", "Rooms", "Summary"});
        JComboBox<String> sortBy = new JComboBox<>(new String[]{"name", "id", "age", "severity", "arrival", "release", "total", "waiting", "ongoing", "confined"});
        JButton genBtn = new JButton("Generate Report");
        JButton saveBtn = new JButton("Save to File");

        styleButton(genBtn);
        styleButton(saveBtn);

        top.add(new JLabel("Report:")); top.add(reportType);
        top.add(new JLabel("Sort By:")); top.add(sortBy);
        top.add(genBtn); top.add(saveBtn);
        p.add(top, BorderLayout.NORTH);

        JTextArea out = new JTextArea();
        out.setEditable(false);
        out.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane outScroll = new JScrollPane(out);
        outScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        p.add(outScroll, BorderLayout.CENTER);

        HospitalReports reports = new HospitalReports(system);

        genBtn.addActionListener(e -> {
            String type = (String) reportType.getSelectedItem();
            String sby = (String) sortBy.getSelectedItem();
            String txt = "";
            switch (type) {
                case "Department Summary": txt = reports.generateDepartmentReport(sby); break;
                case "Patients (ER)": txt = reports.generatePatientReport("er", sby); break;
                case "Patients (Released)": txt = reports.generatePatientReport("released", sby); break;
                case "Patients (All)": txt = reports.generatePatientReport("all", sby); break;
                case "Rooms": txt = reports.generateRoomReport(); break;
                case "Summary": txt = reports.generateSummaryReport(); break;
            }
            out.setText(txt);
        });

        saveBtn.addActionListener(e -> {
            String content = out.getText();
            if (content == null || content.isEmpty()) { JOptionPane.showMessageDialog(this, "Generate a report first."); return; }
            JFileChooser fc = new JFileChooser();
            int res = fc.showSaveDialog(this);
            if (res != JFileChooser.APPROVE_OPTION) return;
            try (java.io.FileWriter fw = new java.io.FileWriter(fc.getSelectedFile())) {
                fw.write(content);
                JOptionPane.showMessageDialog(this, "Saved to " + fc.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
            }
        });

        return p;
    }
    private JScrollPane createDepartmentsPanel() {
        JPanel outer = new JPanel(new GridLayout(0,2,12,12));
        outer.setBackground(BACKGROUND_COLOR);
        outer.setBorder(new EmptyBorder(10,10,10,10));

        for (String deptName : system.getDepartments().keySet()) {
            Department dept = system.getDepartments().get(deptName);

            JPanel card = new JPanel(new BorderLayout(6,6));
            card.setBackground(Color.white);
            card.setBorder(new LineBorder(BORDER_COLOR, 1, true));

            TitledBorder tb = BorderFactory.createTitledBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                deptName,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 16),
                PRIMARY_COLOR
            );
            tb.setTitleJustification(TitledBorder.CENTER);
            card.setBorder(BorderFactory.createCompoundBorder(tb, new EmptyBorder(10,10,10,10)));

            String desc = deptDescriptions.getOrDefault(deptName, "General department services and specialist consultations.");
            card.setToolTipText("<html><b>" + deptName + "</b><br>" + desc + "</html>");

            JPanel subHeaders = new JPanel(new GridLayout(1,2));
            subHeaders.setOpaque(false);
            subHeaders.setBorder(new EmptyBorder(0,0,5,0));
            JLabel queueHeader = new JLabel("FOR QUEUEING", SwingConstants.CENTER);
            queueHeader.setForeground(TEXT_COLOR);
            queueHeader.setFont(queueHeader.getFont().deriveFont(Font.BOLD, 12f));
            JLabel ongoingHeader = new JLabel("ONGOING TREATMENT (Doctor slots)", SwingConstants.CENTER);
            ongoingHeader.setForeground(TEXT_COLOR);
            ongoingHeader.setFont(ongoingHeader.getFont().deriveFont(Font.BOLD, 12f));
            subHeaders.add(queueHeader);
            subHeaders.add(ongoingHeader);
            card.add(subHeaders, BorderLayout.NORTH);

            DefaultTableModel waitModel = new DefaultTableModel(new Object[]{"ID","Name","Case","Severity","Arrival"},0) {
                public boolean isCellEditable(int r,int c){return false;}
            };
            JTable waitTable = new JTable(waitModel);
            waitTable.getColumnModel().getColumn(3).setCellRenderer(new SeverityCellRenderer());
            waitTable.getTableHeader().setBackground(SECONDARY_COLOR);
            waitTable.getTableHeader().setForeground(PRIMARY_COLOR);
            waitTable.setFillsViewportHeight(true);
            JScrollPane waitScroll = new JScrollPane(waitTable);
            waitScroll.setWheelScrollingEnabled(true);
            waitScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
            waitingTables.put(deptName, waitTable);

            DefaultTableModel onModel = new DefaultTableModel(new Object[]{"Slot","Doctor","Patient ID","Case","Severity","Started"},0){
                public boolean isCellEditable(int r,int c){return false;}
            };
            JTable onTable = new JTable(onModel);
            onTable.getColumnModel().getColumn(4).setCellRenderer(new SeverityCellRenderer());
            onTable.getTableHeader().setBackground(SECONDARY_COLOR);
            onTable.getTableHeader().setForeground(PRIMARY_COLOR);
            onTable.setFillsViewportHeight(true);
            JScrollPane onScroll = new JScrollPane(onTable);
            onScroll.setWheelScrollingEnabled(true);
            onScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
            ongoingTables.put(deptName, onTable);

            JPanel center = new JPanel(new GridLayout(1,2,6,6));
            center.setOpaque(false);
            center.add(waitScroll);
            center.add(onScroll);

            JPanel right = new JPanel();
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
            right.setBackground(Color.white);
            right.setBorder(new EmptyBorder(0, 10, 0, 0));

            DefaultListModel<String> docListModel = new DefaultListModel<>();
            JList<String> docList = new JList<>(docListModel);
            docList.setVisibleRowCount(6);
            docList.setCellRenderer(new DoctorListCellRenderer());
            doctorListModels.put(deptName, docListModel);
            doctorJLists.put(deptName, docList);

            JLabel doctorsLabel = new JLabel("Doctors:");
            doctorsLabel.setForeground(PRIMARY_COLOR);
            doctorsLabel.setFont(doctorsLabel.getFont().deriveFont(Font.BOLD, 12f));
            right.add(doctorsLabel);
            JScrollPane docListScroll = new JScrollPane(docList);
            docListScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
            right.add(docListScroll);
            right.add(Box.createRigidArea(new Dimension(0,10)));

            JButton releaseBtn = new JButton("Release (Selected Ongoing)");
            JButton confineBtn = new JButton("Confinement");

            styleButton(releaseBtn);
            styleButton(confineBtn);

            right.add(releaseBtn);
            right.add(Box.createRigidArea(new Dimension(0,6)));
            right.add(confineBtn);

            releaseBtn.addActionListener(e -> {
                int sel = onTable.getSelectedRow();
                if (sel < 0) { JOptionPane.showMessageDialog(this, "Select an ongoing row to release."); return; }
                int patientId = (int) onTable.getValueAt(sel, 2);
                boolean ok = system.releaseOngoingPatient(deptName, patientId);
                if (!ok) { JOptionPane.showMessageDialog(this,"Release failed."); return; }
                Toolkit.getDefaultToolkit().beep();
                refreshAll();
            });

            confineBtn.addActionListener(e -> {
                int sel = onTable.getSelectedRow();
                if (sel < 0) { JOptionPane.showMessageDialog(this, "Select an ongoing row to confine."); return; }
                int patientId = (int) onTable.getValueAt(sel, 2);

                JComboBox<String> roomBox = new JComboBox<>();
                for (int r = 1; r <= 101; r++) roomBox.addItem("Room " + r);
                roomBox.setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        String s = (value == null) ? "" : value.toString();
                        int rn = Integer.parseInt(s.replace("Room ",""));
                        if (system.isRoomOccupied(rn)) c.setForeground(Color.RED);
                        else c.setForeground(ACCENT_COLOR);
                        return c;
                    }
                });

                JComboBox<Integer> daysBox = new JComboBox<>();
                for(int d=0; d<=30; d++) daysBox.addItem(d);
                JComboBox<Integer> monthsBox = new JComboBox<>();
                for(int m=0; m<=12; m++) monthsBox.addItem(m);
                JComboBox<Integer> yearsBox = new JComboBox<>();
                for(int y=0; y<=10; y++) yearsBox.addItem(y);

                JPanel panel = new JPanel(new GridLayout(4,2,6,6));
                panel.add(new JLabel("Room:")); panel.add(roomBox);
                panel.add(new JLabel("Days:")); panel.add(daysBox);
                panel.add(new JLabel("Months (30d each):")); panel.add(monthsBox);
                panel.add(new JLabel("Years (365d each):")); panel.add(yearsBox);

                int res = JOptionPane.showConfirmDialog(this, panel, "Confinement (Room 1..101)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if(res != JOptionPane.OK_OPTION) return;
                int roomNumber = Integer.parseInt(((String)roomBox.getSelectedItem()).replace("Room ",""));
                if(system.isRoomOccupied(roomNumber)){ JOptionPane.showMessageDialog(this,"Selected room is occupied."); return; }

                int days = (Integer) daysBox.getSelectedItem();
                int months = (Integer) monthsBox.getSelectedItem();
                int years = (Integer) yearsBox.getSelectedItem();
                int totalDays = days + months * 30 + years * 365;
                if (totalDays <= 0) { JOptionPane.showMessageDialog(this, "Select a positive duration for confinement."); return; }

                boolean ok = system.confineOngoingPatient(deptName, patientId, roomNumber, totalDays);
                if(!ok){ JOptionPane.showMessageDialog(this,"Confinement failed."); return; }

                Toolkit.getDefaultToolkit().beep();
                refreshAll();
            });

            card.add(center, BorderLayout.CENTER);
            card.add(right, BorderLayout.EAST);
            outer.add(card);
        }

        JScrollPane sc = new JScrollPane(outer);
        sc.setWheelScrollingEnabled(true);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        sc.setBorder(BorderFactory.createEmptyBorder());
        return sc;
    }

    private JPanel createConfinedPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setBackground(BACKGROUND_COLOR);
        confinedModel = new DefaultTableModel(new Object[]{"ID","Name","Age","Case","Severity","Dept","Arrival","Room","Days","ConfinementInfo"},0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        confinedTable = new JTable(confinedModel);
        confinedTable.getColumnModel().getColumn(4).setCellRenderer(new SeverityCellRenderer());
        confinedTable.getTableHeader().setBackground(SECONDARY_COLOR);
        confinedTable.getTableHeader().setForeground(PRIMARY_COLOR);
        confinedTable.setFillsViewportHeight(true);
        confinedTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        if (confinedTable.getColumnModel().getColumnCount() >= 10) {
            confinedTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            confinedTable.getColumnModel().getColumn(1).setPreferredWidth(150);
            confinedTable.getColumnModel().getColumn(2).setPreferredWidth(50);
            confinedTable.getColumnModel().getColumn(3).setPreferredWidth(150);
            confinedTable.getColumnModel().getColumn(4).setPreferredWidth(90);
            confinedTable.getColumnModel().getColumn(5).setPreferredWidth(140);
            confinedTable.getColumnModel().getColumn(6).setPreferredWidth(110);
            confinedTable.getColumnModel().getColumn(7).setPreferredWidth(70);
            confinedTable.getColumnModel().getColumn(8).setPreferredWidth(60);
            confinedTable.getColumnModel().getColumn(9).setPreferredWidth(360);
        }
        if (confinedTable.getColumnModel().getColumnCount() >= 10) {
            confinedTable.getColumnModel().getColumn(9).setCellRenderer(createTooltipRenderer());
        }
        JScrollPane confScroll = new JScrollPane(confinedTable);
        confScroll.setWheelScrollingEnabled(true);
        confScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        confScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        p.add(confScroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        bottom.setOpaque(false);
        JButton dischargeBtn = new JButton("Release Selected (Discharge)");
        JButton extendBtn = new JButton("Extend Selected Confinement");
        styleButton(dischargeBtn);
        styleButton(extendBtn);
        bottom.add(dischargeBtn);
        bottom.add(Box.createRigidArea(new Dimension(6,0)));
        bottom.add(extendBtn);
        p.add(bottom, BorderLayout.SOUTH);

        dischargeBtn.addActionListener(e -> doReleaseConfinedSelected());
        confinedTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e){
                if(e.getClickCount()==2) doReleaseConfinedSelected();
            }
        });

        extendBtn.addActionListener(e -> {
            int sel = confinedTable.getSelectedRow();
            if (sel < 0) { JOptionPane.showMessageDialog(this, "Select a confined patient to extend."); return; }
            int pid = (int) confinedModel.getValueAt(sel, 0);
            String deptFound = null;
            Patient target = null;
            for (String deptName : system.getDepartments().keySet()) {
                Department dept = system.getDepartments().get(deptName);
                for (Patient pt : dept.getConfinedSnapshot()) {
                    if (pt.getId() == pid) { deptFound = deptName; target = pt; break; }
                }
                if (deptFound != null) break;
            }
            if (deptFound == null || target == null) { JOptionPane.showMessageDialog(this, "Could not find confined patient."); return; }

            JComboBox<Integer> addDays = new JComboBox<>();
            for (int d=0; d<=30; d++) addDays.addItem(d);
            JComboBox<Integer> addMonths = new JComboBox<>();
            for (int m=0; m<=12; m++) addMonths.addItem(m);
            JComboBox<Integer> addYears = new JComboBox<>();
            for (int y=0; y<=10; y++) addYears.addItem(y);

            JPanel panel = new JPanel(new GridLayout(3,2,6,6));
            panel.add(new JLabel("Add Days:")); panel.add(addDays);
            panel.add(new JLabel("Add Months (30d each):")); panel.add(addMonths);
            panel.add(new JLabel("Add Years (365d each):")); panel.add(addYears);

            int res = JOptionPane.showConfirmDialog(this, panel, "Extend Confinement", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return;

            int days = (Integer) addDays.getSelectedItem();
            int months = (Integer) addMonths.getSelectedItem();
            int years = (Integer) addYears.getSelectedItem();
            int extraDays = days + months * 30 + years * 365;
            if (extraDays <= 0) { JOptionPane.showMessageDialog(this, "Select a positive extension."); return; }

            target.setConfinementDays(target.getConfinementDays() + extraDays);
            java.time.LocalDateTime est = java.time.LocalDateTime.now().plusDays(target.getConfinementDays());
            String info = "Room " + target.getConfinementRoom() + " - " + target.getConfinementDays() + " day(s) (est release: " + est.toLocalDate().toString() + ")";
            target.setConfinementInfo(info);

            Toolkit.getDefaultToolkit().beep();
            refreshAll();
        });

        return p;
    }

    private void doReleaseConfinedSelected(){
        int sel = confinedTable.getSelectedRow();
        if(sel<0){ JOptionPane.showMessageDialog(this,"Select a confined patient first."); return; }
        int pid = (int)confinedModel.getValueAt(sel,0);
        String deptFound = null;
        for(String deptName: system.getDepartments().keySet()){
            Department dept = system.getDepartments().get(deptName);
            for(Patient ptn : dept.getConfinedSnapshot()){
                if(ptn.getId()==pid){ deptFound = deptName; break; }
            }
            if(deptFound!=null) break;
        }
        if(deptFound==null){ JOptionPane.showMessageDialog(this,"Could not find confined patient location."); return; }
        boolean ok = system.releaseConfinedPatient(deptFound, pid);
        if(!ok){ JOptionPane.showMessageDialog(this,"Discharge failed."); return; }
        Toolkit.getDefaultToolkit().beep();
        refreshAll();
    }

    private JPanel createRecordsPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setBackground(BACKGROUND_COLOR);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setOpaque(false);
        recordsSearchField = new JTextField(30);
        recordsSearchField.setBorder(new LineBorder(BORDER_COLOR, 1));
        JButton searchBtn = new JButton("Search");
        JButton exportBtn = new JButton("Export Released (CSV)");
        styleButton(searchBtn);
        styleButton(exportBtn);

        JLabel searchLabel = new JLabel("Search released (ID or name):");
        searchLabel.setForeground(TEXT_COLOR);
        top.add(searchLabel);
        top.add(recordsSearchField);
        top.add(searchBtn);
        top.add(exportBtn);
        p.add(top, BorderLayout.NORTH);

        recordsModel = new DefaultTableModel(new Object[]{"ID","Name","Age","Case","Dept","Status","Arrived","ReleasedAt","ConfinementInfo"},0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        JTable recTable = new JTable(recordsModel);
        recTable.getTableHeader().setBackground(SECONDARY_COLOR);
        recTable.getTableHeader().setForeground(PRIMARY_COLOR);
        recTable.setFillsViewportHeight(true);
        recTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        if (recTable.getColumnModel().getColumnCount() >= 8) {
            recTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            recTable.getColumnModel().getColumn(1).setPreferredWidth(160);
            recTable.getColumnModel().getColumn(2).setPreferredWidth(50);
            recTable.getColumnModel().getColumn(3).setPreferredWidth(140);
            recTable.getColumnModel().getColumn(4).setPreferredWidth(140);
            recTable.getColumnModel().getColumn(5).setPreferredWidth(90);
            recTable.getColumnModel().getColumn(6).setPreferredWidth(120);
            recTable.getColumnModel().getColumn(7).setPreferredWidth(360);
        }
        JScrollPane recScroll = new JScrollPane(recTable);
        recScroll.setWheelScrollingEnabled(true);
        recScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        recScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        p.add(recScroll, BorderLayout.CENTER);
        if (recTable.getColumnModel().getColumnCount() >= 8) {
            recTable.getColumnModel().getColumn(6).setCellRenderer(createTooltipRenderer());
            recTable.getColumnModel().getColumn(7).setCellRenderer(createTooltipRenderer());
        }

        searchBtn.addActionListener(e -> {
            String q = recordsSearchField.getText();
            List<Patient> results = system.searchReleasedPatients(q);
            recordsModel.setRowCount(0);
            for(Patient pt : results){
                String arrived = pt.getArrivalTime()==null?"":pt.getArrivalTime().format(releaseDtf);
                String released = pt.getReleaseTime()==null?"":pt.getReleaseTime().format(releaseDtf);
                recordsModel.addRow(new Object[]{pt.getId(), pt.getName(), pt.getAge(), pt.getCaseDescription(), pt.getDepartment(), pt.getStatus().name(), arrived, released, pt.getConfinementInfo()});
            }
        });

        recordsSearchField.addKeyListener(new KeyAdapter(){
            public void keyReleased(KeyEvent e){
                if(e.getKeyCode()==KeyEvent.VK_ENTER) searchBtn.doClick();
            }
        });

        exportBtn.addActionListener(e -> {
            try{
                String csv = system.exportReleasedPatientsToCSV();
                String filename = "released_records.csv";
                try(FileWriter fw = new FileWriter(filename)){ fw.write(csv); }
                JOptionPane.showMessageDialog(this,"Exported to "+filename);
            }catch(Exception ex){
                JOptionPane.showMessageDialog(this,"Export failed: "+ex.getMessage());
            }
        });

        return p;
    }

    private JPanel createStatsPanel() {
        statsPanel = new JPanel(new GridLayout(0,1,6,6));
        statsPanel.setBackground(BACKGROUND_COLOR);
        statsPanel.setBorder(new EmptyBorder(12,12,12,12));
        return statsPanel;
    }

    private void styleButton(JButton button) {
        button.setBackground(ACCENT_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ACCENT_COLOR.darker(), 1, true),
            new EmptyBorder(5, 15, 5, 15)
        ));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ACCENT_COLOR.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(ACCENT_COLOR);
            }
        });
    }

    private Patient createRandomPatient() {
        Random r = new Random();
        String name = PATIENT_NAMES[r.nextInt(PATIENT_NAMES.length)];
        int age = 10 + r.nextInt(70);
        String cs = SAMPLE_CASES_ARRAY[r.nextInt(SAMPLE_CASES_ARRAY.length)];
        Patient.Severity[] vals = Patient.Severity.values();
        Patient.Severity s = vals[r.nextInt(vals.length)];
        return new Patient(name, age, cs, s, LocalDateTime.now());
    }

    private static class SeverityCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column){
            Component comp = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
            if(value!=null){
                String sev = value.toString();
                Color bg = Color.WHITE;
                if(sev.equalsIgnoreCase("VERY_MINOR") || sev.equalsIgnoreCase("MILD")) bg = new Color(198,239,206);
                else if(sev.equalsIgnoreCase("MODERATE")) bg = new Color(255,235,156);
                else if(sev.equalsIgnoreCase("SEVERE")) bg = new Color(255,199,206);
                else if(sev.equalsIgnoreCase("CRITICAL")) bg = new Color(255,120,120);
                comp.setBackground(isSelected ? table.getSelectionBackground() : bg);
            } else comp.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return comp;
        }
    }

    private class DoctorListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof String) {
                String text = (String) value;
                if (text.contains("Open Slot")) {
                    c.setForeground(ACCENT_COLOR.darker());
                } else if (text.contains("Treating:")) {
                    c.setForeground(Color.RED.darker());
                } else if (text.contains("Busy")) {
                    c.setForeground(Color.RED.darker());
                } else {
                    c.setForeground(TEXT_COLOR);
                }
            }
            return c;
        }
    }

    private void refreshAll(){
        if(erModel!=null){
            erModel.setRowCount(0);
            PriorityQueue<Patient> copy = new PriorityQueue<>(system.getTriageQueue().getQueue());
            while(!copy.isEmpty()){
                Patient p = copy.poll();
                erModel.addRow(new Object[]{p.getId(),p.getName(),p.getAge(),p.getCaseDescription(),p.getSeverity().name(),p.getArrivalTime().format(dtf)});
            }
        }

        for(String deptName: system.getDepartments().keySet()){
            Department dept = system.getDepartments().get(deptName);

            JTable wTable = waitingTables.get(deptName);
            DefaultTableModel wModel = (DefaultTableModel)wTable.getModel();
            wModel.setRowCount(0);
            for(Patient p: dept.getWaitingSnapshotSorted()){
                wModel.addRow(new Object[]{p.getId(),p.getName(),p.getCaseDescription(),p.getSeverity().name(),p.getArrivalTime().format(dtf)});
            }

            JTable oTable = ongoingTables.get(deptName);
            DefaultTableModel oModel = (DefaultTableModel)oTable.getModel();
            oModel.setRowCount(0);
            List<Department.OngoingEntry> ongoing = dept.getOngoingSnapshot();
            int slot=1;
            for(Department.OngoingEntry entry: ongoing){
                Patient p = entry.getPatient();
                Doctor d = entry.getDoctor();
                oModel.addRow(new Object[]{slot,d.getName(),p.getId(),p.getCaseDescription(),p.getSeverity().name(),entry.getStart().format(dtf)});
                slot++;
            }
            for(;slot<=Department.MAX_ONGOING;slot++){
                oModel.addRow(new Object[]{slot,"(Open Slot)","","","","",""});
            }

            DefaultListModel<String> dlm = doctorListModels.get(deptName);
            dlm.clear();
            List<Doctor> pool = system.getDoctorsForDept(deptName);
            for(Doctor doc: pool){
                String extra = "";
                for(Department.OngoingEntry entry: ongoing){
                    if(entry.getDoctor().getId()==doc.getId()){
                        extra = " - Treating: " + entry.getPatient().getName();
                        break;
                    }
                }
                String display = doctorDisplayNames.get(doc.getId());
                if (display == null) {
                    display = generateDoctorName();
                    doctorDisplayNames.put(doc.getId(), display);
                }
                dlm.addElement(display + extra);
            }
        }

        if(confinedModel!=null){
            confinedModel.setRowCount(0);
            for(String deptName: system.getDepartments().keySet()){
                Department dept = system.getDepartments().get(deptName);
                for(Patient p: dept.getConfinedSnapshot()){
                    confinedModel.addRow(new Object[]{p.getId(),p.getName(),p.getAge(),p.getCaseDescription(),p.getSeverity().name(),deptName,p.getArrivalTime().format(dtf),p.getConfinementRoom(),p.getConfinementDays(),p.getConfinementInfo()});
                }
            }
        }

        if(recordsModel!=null){
            recordsModel.setRowCount(0);
            for(Patient p: system.getReleasedPatients()){
                String arrived = p.getArrivalTime() == null ? "" : p.getArrivalTime().format(releaseDtf);
                String released = p.getReleaseTime() == null ? "" : p.getReleaseTime().format(releaseDtf);
                recordsModel.addRow(new Object[]{p.getId(), p.getName(), p.getAge(), p.getCaseDescription(), p.getDepartment(), p.getStatus().name(), arrived, released, p.getConfinementInfo()});
            }
        }

        refreshStats();
    }

    private void refreshStats(){
        if(statsPanel==null) return;
        statsPanel.removeAll();
        Map<String,Integer> stats = system.getStats();
        for(Map.Entry<String,Integer> e: stats.entrySet()){
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            JLabel title = new JLabel(e.getKey());
            title.setFont(title.getFont().deriveFont(Font.BOLD,14f));
            title.setForeground(TEXT_COLOR);
            JLabel value = new JLabel(String.valueOf(e.getValue()),SwingConstants.RIGHT);
            value.setFont(value.getFont().deriveFont(Font.PLAIN,14f));
            value.setForeground(PRIMARY_COLOR);
            row.add(title,BorderLayout.WEST);
            row.add(value,BorderLayout.EAST);
            row.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_COLOR));
            statsPanel.add(row);
        }
        statsPanel.revalidate();
        statsPanel.repaint();
    }

    @Override
    public void onLog(String message){
        SwingUtilities.invokeLater(() -> refreshAll());
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new HospitalGUI());
    }
}
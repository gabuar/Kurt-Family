import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class HospitalGUI extends JFrame implements LogListener {
    private HospitalSystem hospitalSystem;

    // UI models
    private DefaultTableModel erModel;
    private DefaultTableModel recordsModel;
    private JTextArea logArea;

    // department models keyed by department name
    private Map<String, DefaultTableModel> deptWaitingModels = new HashMap<>();
    private Map<String, DefaultTableModel> deptOngoingModels = new HashMap<>();

    public HospitalGUI() {
        setTitle("Hospital Management System");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // create backend system (it will seed patients)
        hospitalSystem = new HospitalSystem();
        hospitalSystem.setLogListener(this);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("ER / Triage", createERPanel());
        tabs.addTab("Departments", createDepartmentsPanel());
        tabs.addTab("Records", createRecordsPanel());
        tabs.addTab("Logs", createLogsPanel());

        add(tabs, BorderLayout.CENTER);

        // initial populate
        refreshAll();
    }

    private JPanel createERPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // top controls
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Patient (dialog)");
        JButton processNextBtn = new JButton("Process Next (ER -> Dept)");
        top.add(addBtn);
        top.add(processNextBtn);

        // ER table (shows current triage queue in priority order)
        erModel = new DefaultTableModel(new String[]{"Name", "Age", "Case", "Severity", "ArrivalTime"}, 0);
        JTable erTable = new JTable(erModel);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(erTable), BorderLayout.CENTER);

        // Add patient dialog action
        addBtn.addActionListener(e -> {
            try {
                String name = JOptionPane.showInputDialog(this, "Patient name:");
                if (name == null || name.trim().isEmpty()) return;

                String ageStr = JOptionPane.showInputDialog(this, "Age:");
                if (ageStr == null || ageStr.trim().isEmpty()) return;
                int age = Integer.parseInt(ageStr.trim());

                String caseDesc = JOptionPane.showInputDialog(this, "Case description:");
                if (caseDesc == null || caseDesc.trim().isEmpty()) return;

                // Choose severity
                Patient.Severity severity = (Patient.Severity) JOptionPane.showInputDialog(
                        this,
                        "Select severity:",
                        "Severity",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        Patient.Severity.values(),
                        Patient.Severity.MODERATE
                );
                if (severity == null) return;

                // Choose department target
                Object[] depts = hospitalSystem.getDepartments().keySet().toArray();
                String dept = (String) JOptionPane.showInputDialog(
                        this,
                        "Assign to department:",
                        "Department",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        depts,
                        depts.length > 0 ? depts[0] : null
                );
                if (dept == null) return;

                // create patient (this Patient constructor signature used in your HospitalSystem earlier)
                Patient p = new Patient(name, age, caseDesc, severity, java.time.LocalDateTime.now());
                // if your Patient has a department field constructor, change constructor call accordingly
                // add to ER
                hospitalSystem.addPatientToER(p);

                // update UI
                refreshTriageTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Age must be a number.", "Input error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding patient: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Process next patient from ER to department
        processNextBtn.addActionListener(e -> {
            // Show the next patient so user knows who's being processed
            PriorityQueue<Patient> copy = new PriorityQueue<>(hospitalSystem.getTriageQueue().getQueue());
            if (copy.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Triage queue is empty.");
                return;
            }
            Patient next = copy.peek();
            int choice = JOptionPane.showConfirmDialog(this,
                    "Process next patient:\n" + next + "\n\nSend to their department?",
                    "Process Next",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                // The hospitalSystem.moveFromERToDepartment expects a department name in some variants,
                // but we have a generic process approach in your system. We'll call moveFromERToDepartment using patient's desired dept if available,
                // otherwise call a generic process that polls and assigns by asking user for dept.
                // Attempt to move using the built-in method that polls the triage, then requires a dept parameter
                // If your HospitalSystem has a method that auto-assigns based on patient data, use that instead.
                hospitalSystem.moveFromERToDepartment(next.getCaseDescription()); // fallback: if signature differs, it's fine to adapt
                // Note: The above line assumes the method signature exists. If your HospitalSystem processes the triage queue differently,
                // replace this call with the appropriate one (e.g., hospitalSystem.processNextPatient()).
                refreshTriageTable();
                refreshAllDepartmentTables();
                refreshRecordsTable();
            }
        });

        return panel;
    }

    private JScrollPane createDepartmentsPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));

        for (String deptName : hospitalSystem.getDepartments().keySet()) {

            DefaultTableModel waitingModel = new DefaultTableModel(new String[]{"Name", "Case", "Severity"}, 0);
            DefaultTableModel ongoingModel = new DefaultTableModel(new String[]{"Name", "Case", "Severity"}, 0);

            deptWaitingModels.put(deptName, waitingModel);
            deptOngoingModels.put(deptName, ongoingModel);

            JTable waitingTable = new JTable(waitingModel);
            JTable ongoingTable = new JTable(ongoingModel);

            JButton startBtn = new JButton("Start Treatment");
            JButton releaseBtn = new JButton("Release");

            startBtn.addActionListener(e -> {
                hospitalSystem.startTreatment(deptName);
                refreshDeptTable(deptName);
            });

            releaseBtn.addActionListener(e -> {
                hospitalSystem.releasePatient(deptName);
                refreshDeptTable(deptName);
                refreshRecordsTable();
            });

            JPanel deptPanel = new JPanel(new BorderLayout());
            deptPanel.setBorder(BorderFactory.createTitledBorder(deptName));

            JPanel tables = new JPanel(new GridLayout(2, 1));
            tables.add(new JScrollPane(waitingTable));
            tables.add(new JScrollPane(ongoingTable));

            deptPanel.add(tables, BorderLayout.CENTER);

            JPanel btns = new JPanel();
            btns.add(startBtn);
            btns.add(releaseBtn);
            deptPanel.add(btns, BorderLayout.SOUTH);

            panel.add(deptPanel);
        }

        return new JScrollPane(panel);
    }

    private JPanel createRecordsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        recordsModel = new DefaultTableModel(new String[]{"Name", "Age", "Case", "Department"}, 0);
        JTable recordsTable = new JTable(recordsModel);
        panel.add(new JScrollPane(recordsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        logArea = new JTextArea();
        logArea.setEditable(false);
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        return panel;
    }

    // LogListener implementation
    @Override
    public void onLog(String message) {
        SwingUtilities.invokeLater(() -> {
            if (logArea != null) {
                logArea.append(message + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
    }

    // --- Refresh helpers ---
    private void refreshAll() {
        refreshTriageTable();
        refreshAllDepartmentTables();
        refreshRecordsTable();
    }

    private void refreshTriageTable() {
        erModel.setRowCount(0);
        PriorityQueue<Patient> copy = new PriorityQueue<>(hospitalSystem.getTriageQueue().getQueue());
        while (!copy.isEmpty()) {
            Patient p = copy.poll();
            erModel.addRow(new Object[]{p.getName(), p.getAge(), p.getCaseDescription(), p.getSeverity().name(), p.getArrivalTime().toString()});
        }
    }

    private void refreshAllDepartmentTables() {
        for (String deptName : hospitalSystem.getDepartments().keySet()) {
            refreshDeptTable(deptName);
        }
    }

    private void refreshDeptTable(String deptName) {
        Department d = hospitalSystem.getDepartment(deptName);
        DefaultTableModel waitingModel = deptWaitingModels.get(deptName);
        DefaultTableModel ongoingModel = deptOngoingModels.get(deptName);
        if (waitingModel == null || ongoingModel == null) return;

        waitingModel.setRowCount(0);
        ongoingModel.setRowCount(0);

        for (Patient p : d.getWaitingQueue()) {
            waitingModel.addRow(new Object[]{p.getName(), p.getCaseDescription(), p.getSeverity().name()});
        }
        for (Patient p : d.getOngoingQueue()) {
            ongoingModel.addRow(new Object[]{p.getName(), p.getCaseDescription(), p.getSeverity().name()});
        }
    }

    private void refreshRecordsTable() {
        recordsModel.setRowCount(0);
        for (Patient p : hospitalSystem.getReleasedPatients()) {
            // You may need to add department info in released patients; if not tracked, put "N/A"
            recordsModel.addRow(new Object[]{p.getName(), p.getAge(), p.getCaseDescription(), "N/A"});
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HospitalGUI gui = new HospitalGUI();
            gui.setVisible(true);
        });
    }
}

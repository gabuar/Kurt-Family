import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class HospitalGUI extends JFrame {
    private HospitalSystem hospital;

    // ER Triage tables
    private DefaultTableModel arrivalModel;
    private JTable arrivalTable;

    // Input fields
    private JTextField nameField, ageField, caseField;
    private JComboBox<Patient.Severity> severityBox;

    public HospitalGUI() {
        hospital = new HospitalSystem();

        setTitle("Hospital Management System");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("ER Triage", createTriagePanel());
        tabs.add("Departments", createDepartmentsPanel());
        tabs.add("Records", createRecordsPanel());

        add(tabs);
    }

    // ER TRIAGE
    private JPanel createTriagePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Form
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        nameField = new JTextField();
        ageField = new JTextField();
        caseField = new JTextField();
        severityBox = new JComboBox<>(Patient.Severity.values());
        JButton letInBtn = new JButton("Let In");

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Age:"));
        formPanel.add(ageField);
        formPanel.add(new JLabel("Case:"));
        formPanel.add(caseField);
        formPanel.add(new JLabel("Severity:"));
        formPanel.add(severityBox);
        formPanel.add(new JLabel(""));
        formPanel.add(letInBtn);

        // Table
        arrivalModel = new DefaultTableModel(new String[]{"Name", "Age", "Case", "Severity", "Time"}, 0);
        arrivalTable = new JTable(arrivalModel);
        JScrollPane scroll = new JScrollPane(arrivalTable);

        // Load default ER patients
        for (Patient p : hospital.getTriage().getQueue()) {
            arrivalModel.addRow(new Object[]{
                    p.getName(),
                    p.getAge(),
                    p.getCaseDescription(),
                    p.getSeverity().name(),
                    p.getArrivalTime().toString()
            });
        }

        // Action
        letInBtn.addActionListener(e -> {
            String name = nameField.getText();
            String ageStr = ageField.getText();
            String caseDesc = caseField.getText();
            Patient.Severity severity = (Patient.Severity) severityBox.getSelectedItem();

            if (!name.isEmpty() && !ageStr.isEmpty() && !caseDesc.isEmpty()) {
                try {
                    int age = Integer.parseInt(ageStr);
                    Patient patient = new Patient(
                            name,
                            age,
                            caseDesc,
                            severity,
                            java.time.LocalDateTime.now()
                    );

                    hospital.addPatient(patient);

                    arrivalModel.addRow(new Object[]{
                            patient.getName(),
                            patient.getAge(),
                            patient.getCaseDescription(),
                            patient.getSeverity().name(),
                            patient.getArrivalTime().toString()
                    });

                    nameField.setText("");
                    ageField.setText("");
                    caseField.setText("");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Age must be a number!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please fill all fields!");
            }
        });

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // Departments Tab
    private JPanel createDepartmentsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        for (String deptName : hospital.getDepartments().keySet()) {
            Department dept = hospital.getDepartment(deptName);

            DefaultTableModel model = new DefaultTableModel(
                    new String[]{"Name", "Age", "Case", "Severity", "Arrival"}, 0);
            JTable table = new JTable(model);

            // Load default patients
            for (Patient p : dept.getWaitingQueue()) {
                model.addRow(new Object[]{
                        p.getName(),
                        p.getAge(),
                        p.getCaseDescription(),
                        p.getSeverity().name(),
                        p.getArrivalTime().toString()
                });
            }

            JPanel deptPanel = new JPanel(new BorderLayout());
            deptPanel.setBorder(BorderFactory.createTitledBorder(deptName));
            deptPanel.add(new JScrollPane(table), BorderLayout.CENTER);

            panel.add(deptPanel);
        }

        return panel;
    }

    // Records Tab (placeholder for now)
    private JPanel createRecordsPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Records Page (to be implemented)"));
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HospitalGUI().setVisible(true));
    }
}

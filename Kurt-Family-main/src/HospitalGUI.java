import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class HospitalGUI extends JFrame implements LogListener {

    private final HospitalSystem system;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    // ER
    private JTable erTable;
    private DefaultTableModel erModel;

    // Depts
    private final Map<String, JTable> waitingTables = new LinkedHashMap<>();
    private final Map<String, JTable> ongoingTables = new LinkedHashMap<>();
    private final Map<String, DefaultListModel<String>> doctorListModels = new LinkedHashMap<>();
    private final Map<String, JList<String>> doctorJLists = new LinkedHashMap<>();

    // Confined
    private JTable confinedTable;
    private DefaultTableModel confinedModel;

    // Records
    private DefaultTableModel recordsModel;
    private JTextField recordsSearchField;

    // Logs & Stats
    private JTextArea logArea;
    private JPanel statsPanel;

    private static final String[] PATIENT_NAMES = {
        "Meredith Grey", "Luke Skywalker", "Leia Organa", "Tony Stark", "Bruce Wayne", "Clark Kent",
        "Diana Prince", "Harry Potter", "Hermione Granger", "Katniss Everdeen", "John Wick", "Ellen Ripley",
        "Neo Anderson", "Trinity", "Jack Sparrow", "Marty McFly", "Sarah Connor", "Forrest Gump", "Simba", "Aragorn"
    };

    private static final String[] SAMPLE_CASES = {"Fever", "Cough", "Chest Pain", "Broken Arm", "Headache", "Asthma", "Pregnancy Pain", "Flu", "Accident Wound", "Dizziness"};

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
        getContentPane().setBackground(new Color(245,250,252));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(235,245,250));

        tabs.add("ER", createERPanel());
        tabs.add("Departments", createDepartmentsPanel());
        tabs.add("Confined", createConfinedPanel());
        tabs.add("Records", createRecordsPanel());
        tabs.add("Stats", createStatsPanel());
        tabs.add("Logs", createLogsPanel());

        add(tabs, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        refreshAll();
        setVisible(true);
    }

    // ---------------- ER ----------------
    private JPanel createERPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setBackground(new Color(245,250,252));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);

        JButton addRandom = new JButton("Newly Arrived Patient");
        JButton addManual = new JButton("Manually Add Patient");
        JButton moveToDept = new JButton("Move Patient to Dept");
        JButton assignSev = new JButton("Manual Assign Severity");

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

        JScrollPane erScroll = new JScrollPane(erTable);
        erScroll.setWheelScrollingEnabled(true);
        p.add(erScroll, BorderLayout.CENTER);

        addRandom.addActionListener(e -> { system.addPatientToER(createRandomPatient()); refreshAll(); });

        addManual.addActionListener(e -> {
            try {
                String name = JOptionPane.showInputDialog(this, "Name:");
                if (name == null) return;
                String ageStr = JOptionPane.showInputDialog(this, "Age:");
                if (ageStr == null) return;
                int age = Integer.parseInt(ageStr.trim());
                String cs = JOptionPane.showInputDialog(this, "Case:");
                if (cs == null) return;
                Patient.Severity sev = (Patient.Severity) JOptionPane.showInputDialog(
                        this, "Severity:", "Severity", JOptionPane.PLAIN_MESSAGE, null, Patient.Severity.values(), Patient.Severity.MODERATE
                );
                if (sev == null) return;
                system.addPatientToER(new Patient(name, age, cs, sev, LocalDateTime.now()));
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
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

    // ---------------- Departments ----------------
    private JScrollPane createDepartmentsPanel() {
        JPanel outer = new JPanel(new GridLayout(0,2,12,12));
        outer.setBackground(new Color(245,250,252));

        for (String deptName : system.getDepartments().keySet()) {
            Department dept = system.getDepartments().get(deptName);

            JPanel card = new JPanel(new BorderLayout(6,6));
            card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200,220,230)), BorderFactory.createEmptyBorder(6,6,6,6)));
            card.setBackground(Color.white);

            JLabel title = new JLabel(deptName, SwingConstants.LEFT);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
            card.add(title, BorderLayout.NORTH);

            JPanel subHeaders = new JPanel(new GridLayout(1,2));
            subHeaders.setOpaque(false);
            subHeaders.add(new JLabel("FOR QUEUEING", SwingConstants.CENTER));
            subHeaders.add(new JLabel("ONGOING TREATMENT (Doctor slots)", SwingConstants.CENTER));
            card.add(subHeaders, BorderLayout.BEFORE_FIRST_LINE);

            // waiting
            DefaultTableModel waitModel = new DefaultTableModel(new Object[]{"ID","Name","Case","Severity","Arrival"},0) {
                public boolean isCellEditable(int r,int c){return false;}
            };
            JTable waitTable = new JTable(waitModel);
            waitTable.getColumnModel().getColumn(3).setCellRenderer(new SeverityCellRenderer());
            JScrollPane waitScroll = new JScrollPane(waitTable);
            waitScroll.setWheelScrollingEnabled(true);
            waitingTables.put(deptName, waitTable);

            // ongoing
            DefaultTableModel onModel = new DefaultTableModel(new Object[]{"Slot","Doctor","Patient ID","Patient Name","Case","Severity","Started"},0){
                public boolean isCellEditable(int r,int c){return false;}
            };
            JTable onTable = new JTable(onModel);
            onTable.getColumnModel().getColumn(5).setCellRenderer(new SeverityCellRenderer());
            JScrollPane onScroll = new JScrollPane(onTable);
            onScroll.setWheelScrollingEnabled(true);
            ongoingTables.put(deptName, onTable);

            JPanel center = new JPanel(new GridLayout(1,2,6,6));
            center.add(waitScroll);
            center.add(onScroll);

            JPanel right = new JPanel();
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
            right.setBackground(Color.white);
            DefaultListModel<String> docListModel = new DefaultListModel<>();
            JList<String> docList = new JList<>(docListModel);
            docList.setVisibleRowCount(6);
            doctorListModels.put(deptName, docListModel);
            doctorJLists.put(deptName, docList);

            JButton autoFillBtn = new JButton("Auto-Fill");
            JButton releaseBtn = new JButton("Release (Selected Ongoing)");
            JButton confineBtn = new JButton("Confinement");
            JButton toggleDocBtn = new JButton("Toggle Selected Doc Availability");
            JButton addDocBtn = new JButton("Add Doctor");

            right.add(new JLabel("Doctors:"));
            right.add(new JScrollPane(docList));
            right.add(Box.createRigidArea(new Dimension(0,6)));
            right.add(toggleDocBtn);
            right.add(addDocBtn);
            right.add(Box.createRigidArea(new Dimension(0,6)));
            right.add(autoFillBtn);
            right.add(Box.createRigidArea(new Dimension(0,6)));
            right.add(releaseBtn);
            right.add(Box.createRigidArea(new Dimension(0,6)));
            right.add(confineBtn);

            // actions
            autoFillBtn.addActionListener(e -> { system.autoFillDepartment(deptName); refreshAll(); });

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
                        else c.setForeground(new Color(10,90,140));
                        return c;
                    }
                });

                JComboBox<Integer> daysBox = new JComboBox<>();
                for(int d=1; d<=30; d++) daysBox.addItem(d);

                JPanel panel = new JPanel(new GridLayout(2,2,6,6));
                panel.add(new JLabel("Room:")); panel.add(roomBox);
                panel.add(new JLabel("Days:")); panel.add(daysBox);

                int res = JOptionPane.showConfirmDialog(this, panel, "Confinement (Room 1..101)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if(res != JOptionPane.OK_OPTION) return;

                int roomNumber = Integer.parseInt(((String)roomBox.getSelectedItem()).replace("Room ",""));
                if(system.isRoomOccupied(roomNumber)){ JOptionPane.showMessageDialog(this,"Selected room is occupied."); return; }

                int days = (Integer)daysBox.getSelectedItem();

                // Call the method with only 4 parameters: deptName, patientId, roomNumber, days
                boolean ok = system.confineOngoingPatient(deptName, patientId, roomNumber, days);
                if(!ok){ JOptionPane.showMessageDialog(this,"Confinement failed."); return; }

                Toolkit.getDefaultToolkit().beep();
                refreshAll();
            });

            toggleDocBtn.addActionListener(e -> {
                int idx = docList.getSelectedIndex();
                if (idx<0) { JOptionPane.showMessageDialog(this,"Select a doctor first."); return; }
                List<Doctor> pool = system.getDoctorsForDept(deptName);
                if(idx>=pool.size()) return;
                Doctor doc = pool.get(idx);
                system.toggleDoctorAvailability(deptName, doc.getId(), !doc.isAvailable());
                refreshAll();
            });

            addDocBtn.addActionListener(e -> {
                String name = JOptionPane.showInputDialog(this,"Doctor name:");
                if(name==null||name.trim().isEmpty()) return;
                system.addDoctorToDept(deptName,name.trim());
                refreshAll();
            });

            card.add(center, BorderLayout.CENTER);
            card.add(right, BorderLayout.EAST);
            outer.add(card);
        }

        JScrollPane sc = new JScrollPane(outer);
        sc.setWheelScrollingEnabled(true);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        return sc;
    }

    // ---------------- Confined ----------------
    private JPanel createConfinedPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setBackground(new Color(245,250,252));
        confinedModel = new DefaultTableModel(new Object[]{"ID","Name","Age","Case","Severity","Dept","Arrival","Room","Days","ConfinementInfo"},0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        confinedTable = new JTable(confinedModel);
        JScrollPane confScroll = new JScrollPane(confinedTable);
        confScroll.setWheelScrollingEnabled(true);
        p.add(confScroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setOpaque(false);
        JButton dischargeBtn = new JButton("Release Selected (Discharge)");
        bottom.add(dischargeBtn);
        p.add(bottom, BorderLayout.SOUTH);

        dischargeBtn.addActionListener(e -> doReleaseConfinedSelected());
        confinedTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e){
                if(e.getClickCount()==2) doReleaseConfinedSelected();
            }
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

    // ---------------- Records ----------------
    private JPanel createRecordsPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setBackground(new Color(245,250,252));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        recordsSearchField = new JTextField(30);
        JButton searchBtn = new JButton("Search");
        JButton exportBtn = new JButton("Export Released (CSV)");
        top.add(new JLabel("Search released (ID or name):"));
        top.add(recordsSearchField);
        top.add(searchBtn);
        top.add(exportBtn);
        p.add(top, BorderLayout.NORTH);

        recordsModel = new DefaultTableModel(new Object[]{"ID","Name","Age","Case","Dept","Status","ReleasedAt","ConfinementInfo"},0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        JTable recTable = new JTable(recordsModel);
        JScrollPane recScroll = new JScrollPane(recTable);
        recScroll.setWheelScrollingEnabled(true);
        p.add(recScroll, BorderLayout.CENTER);

        searchBtn.addActionListener(e -> {
            String q = recordsSearchField.getText();
            List<Patient> results = system.searchReleasedPatients(q);
            recordsModel.setRowCount(0);
            for(Patient pt : results){
                recordsModel.addRow(new Object[]{pt.getId(), pt.getName(), pt.getAge(), pt.getCaseDescription(), pt.getDepartment(), pt.getStatus().name(), pt.getReleaseTime()==null?"":pt.getReleaseTime().format(dtf), pt.getConfinementInfo()});
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

    // ---------------- Stats ----------------
    private JPanel createStatsPanel() {
        statsPanel = new JPanel(new GridLayout(0,1,6,6));
        statsPanel.setBackground(new Color(245,250,252));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        return statsPanel;
    }

    // ---------------- Logs ----------------
    private JPanel createLogsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(245,250,252));
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setWheelScrollingEnabled(true);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ---------------- Helpers ----------------
    private Patient createRandomPatient() {
        Random r = new Random();
        String name = PATIENT_NAMES[r.nextInt(PATIENT_NAMES.length)];
        int age = 10 + r.nextInt(70);
        String cs = SAMPLE_CASES[r.nextInt(SAMPLE_CASES.length)];
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

    private void refreshAll(){
        // ER
        if(erModel!=null){
            erModel.setRowCount(0);
            PriorityQueue<Patient> copy = new PriorityQueue<>(system.getTriageQueue().getQueue());
            while(!copy.isEmpty()){
                Patient p = copy.poll();
                erModel.addRow(new Object[]{p.getId(),p.getName(),p.getAge(),p.getCaseDescription(),p.getSeverity().name(),p.getArrivalTime().format(dtf)});
            }
        }

        // Departments
        for(String deptName: system.getDepartments().keySet()){
            Department dept = system.getDepartments().get(deptName);

            // waiting
            JTable wTable = waitingTables.get(deptName);
            DefaultTableModel wModel = (DefaultTableModel)wTable.getModel();
            wModel.setRowCount(0);
            for(Patient p: dept.getWaitingSnapshotSorted()){
                wModel.addRow(new Object[]{p.getId(),p.getName(),p.getCaseDescription(),p.getSeverity().name(),p.getArrivalTime().format(dtf)});
            }

            // ongoing
            JTable oTable = ongoingTables.get(deptName);
            DefaultTableModel oModel = (DefaultTableModel)oTable.getModel();
            oModel.setRowCount(0);
            List<Department.OngoingEntry> ongoing = dept.getOngoingSnapshot();
            int slot=1;
            for(Department.OngoingEntry entry: ongoing){
                Patient p = entry.getPatient();
                Doctor d = entry.getDoctor();
                oModel.addRow(new Object[]{slot,d.getName(),p.getId(),p.getName(),p.getCaseDescription(),p.getSeverity().name(),entry.getStart().format(dtf)});
                slot++;
            }
            for(;slot<=Department.MAX_ONGOING;slot++){
                oModel.addRow(new Object[]{slot,"(Open Slot)","","","","",""});
            }

            // doctors list
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
                dlm.addElement(doc.toString() + extra);
            }
        }

        // Confined
        if(confinedModel!=null){
            confinedModel.setRowCount(0);
            for(String deptName: system.getDepartments().keySet()){
                Department dept = system.getDepartments().get(deptName);
                for(Patient p: dept.getConfinedSnapshot()){
                    confinedModel.addRow(new Object[]{p.getId(),p.getName(),p.getAge(),p.getCaseDescription(),p.getSeverity().name(),deptName,p.getArrivalTime().format(dtf),p.getConfinementRoom(),p.getConfinementDays(),p.getConfinementInfo()});
                }
            }
        }

        // Records
        if(recordsModel!=null){
            recordsModel.setRowCount(0);
            for(Patient p: system.getReleasedPatients()){
                recordsModel.addRow(new Object[]{p.getId(),p.getName(),p.getAge(),p.getCaseDescription(),p.getDepartment(),p.getStatus().name(),p.getReleaseTime()==null?"":p.getReleaseTime().format(dtf),p.getConfinementInfo()});
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
            JLabel value = new JLabel(String.valueOf(e.getValue()),SwingConstants.RIGHT);
            value.setFont(value.getFont().deriveFont(Font.PLAIN,14f));
            row.add(title,BorderLayout.WEST);
            row.add(value,BorderLayout.EAST);
            row.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(230,230,230)));
            statsPanel.add(row);
        }
        statsPanel.revalidate();
        statsPanel.repaint();
    }

    @Override
    public void onLog(String message){
        SwingUtilities.invokeLater(() -> {
            if(logArea!=null){
                logArea.append("[" + LocalDateTime.now().format(dtf) + "] " + message + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new HospitalGUI());
    }
}
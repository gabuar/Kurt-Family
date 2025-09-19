import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class HospitalSystem {
    private TriageQueue triage;
    private Map<String, Department> departments;

    public HospitalSystem() {
        triage = new TriageQueue();
        departments = new HashMap<>();

        // Initialize departments
        departments.put("General / Clinic", new Department("General / Clinic"));
        departments.put("Pediatrics", new Department("Pediatrics"));
        departments.put("Neurology", new Department("Neurology"));
        departments.put("Cardiology", new Department("Cardiology"));
        departments.put("Orthopedics", new Department("Orthopedics"));
        departments.put("OB-Gynecology", new Department("OB-Gynecology"));

        // Add default patients
        seedDefaultPatients();
    }

    private void seedDefaultPatients() {
        // ER Patients
        for (int i = 1; i <= 10; i++) {
            Patient p = new Patient(
                "ER Patient " + i,
                20 + i,
                "Emergency case " + i,
                Patient.Severity.values()[i % Patient.Severity.values().length],
                LocalDateTime.now().minusMinutes(i)
            );
            triage.addPatient(p);
        }

        // Department patients
        for (Map.Entry<String, Department> entry : departments.entrySet()) {
            Department dept = entry.getValue();
            for (int i = 1; i <= 10; i++) {
                Patient p = new Patient(
                    entry.getKey() + " Patient " + i,
                    5 + (i * 2),
                    "Case " + i + " (" + entry.getKey() + ")",
                    Patient.Severity.values()[i % Patient.Severity.values().length],
                    LocalDateTime.now().minusMinutes(i * 2)
                );
                dept.addToQueue(p);
            }
        }
    }

    public void addPatient(Patient patient) {
        triage.addPatient(patient);
        System.out.println("New patient added to triage: " + patient);
    }

    public void assignToDepartment(String deptName) {
        if (!triage.isEmpty() && departments.containsKey(deptName)) {
            Patient patient = triage.nextPatient();
            departments.get(deptName).addToQueue(patient);
            System.out.println("Transferred to " + deptName + ": " + patient);
        }
    }

    public Department getDepartment(String deptName) {
        return departments.get(deptName);
    }

    public TriageQueue getTriage() {
        return triage;
    }

    public Map<String, Department> getDepartments() {
        return departments;
    }
}

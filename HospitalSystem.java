import java.time.LocalDateTime;
import java.util.*;

public class HospitalSystem {
    private Map<String, Department> departments;
    private TriageQueue triageQueue;
    private LogListener logListener;
    private List<Patient> releasedPatients;

    public HospitalSystem() {
        departments = new LinkedHashMap<>();
        triageQueue = new TriageQueue();
        releasedPatients = new ArrayList<>();

        String[] deptNames = {"General / Clinic", "Pediatrics", "Neurology", "Cardiology", "Orthopedics", "OB-Gynecology"};
        for (String name : deptNames) {
            departments.put(name, new Department(name));
        }

        addDefaultDepartmentPatients();
        addDefaultTriagePatients();
    }

    private void addDefaultDepartmentPatients() {
        // General
        Department gen = departments.get("General / Clinic");
        gen.addToQueue(new Patient("Alice Santos", 34, "Fever", Patient.Severity.MILD, LocalDateTime.now()));
        gen.addToQueue(new Patient("Marco Reyes", 45, "Back Pain", Patient.Severity.MODERATE, LocalDateTime.now()));
        gen.addToQueue(new Patient("Nina Lopez", 23, "Flu", Patient.Severity.MILD, LocalDateTime.now()));
        gen.addToQueue(new Patient("David Cruz", 38, "Check-up", Patient.Severity.VERY_MINOR, LocalDateTime.now()));
        gen.addToQueue(new Patient("Karen Dela Rosa", 29, "Cough", Patient.Severity.MILD, LocalDateTime.now()));
        gen.addToQueue(new Patient("Leo Navarro", 52, "Chest Discomfort", Patient.Severity.SEVERE, LocalDateTime.now()));
        gen.addToQueue(new Patient("Jasmine Tan", 31, "Headache", Patient.Severity.MILD, LocalDateTime.now()));
        gen.addToQueue(new Patient("Patrick Ong", 40, "Diabetes check", Patient.Severity.MODERATE, LocalDateTime.now()));
        gen.addToQueue(new Patient("Grace Uy", 27, "Dizziness", Patient.Severity.MILD, LocalDateTime.now()));
        gen.addToQueue(new Patient("Bryan Lim", 36, "Abdominal Pain", Patient.Severity.MODERATE, LocalDateTime.now()));

        // Pediatrics
        Department ped = departments.get("Pediatrics");
        ped.addToQueue(new Patient("Sophia Reyes", 5, "Cough", Patient.Severity.MILD, LocalDateTime.now()));
        ped.addToQueue(new Patient("Jacob Cruz", 8, "Fever", Patient.Severity.MODERATE, LocalDateTime.now()));
        ped.addToQueue(new Patient("Ella Santos", 3, "Flu", Patient.Severity.MILD, LocalDateTime.now()));
        ped.addToQueue(new Patient("Liam Gomez", 7, "Asthma", Patient.Severity.SEVERE, LocalDateTime.now()));
        ped.addToQueue(new Patient("Mia Dela Cruz", 2, "Ear Infection", Patient.Severity.MODERATE, LocalDateTime.now()));
        ped.addToQueue(new Patient("Noah Tan", 4, "Chickenpox", Patient.Severity.MILD, LocalDateTime.now()));
        ped.addToQueue(new Patient("Lucas Ong", 6, "Allergy", Patient.Severity.MODERATE, LocalDateTime.now()));
        ped.addToQueue(new Patient("Emma Lim", 1, "Fever", Patient.Severity.SEVERE, LocalDateTime.now()));
        ped.addToQueue(new Patient("Olivia Sy", 9, "Broken Arm", Patient.Severity.SEVERE, LocalDateTime.now()));
        ped.addToQueue(new Patient("Ethan Uy", 10, "Cold", Patient.Severity.MILD, LocalDateTime.now()));

        // Neurology
        Department neuro = departments.get("Neurology");
        neuro.addToQueue(new Patient("Carlos Medina", 60, "Stroke", Patient.Severity.CRITICAL, LocalDateTime.now()));
        neuro.addToQueue(new Patient("Rosa Villanueva", 50, "Seizure", Patient.Severity.SEVERE, LocalDateTime.now()));
        neuro.addToQueue(new Patient("Miguel Santos", 42, "Head Trauma", Patient.Severity.SEVERE, LocalDateTime.now()));
        neuro.addToQueue(new Patient("Anna Flores", 35, "Migraine", Patient.Severity.MODERATE, LocalDateTime.now()));
        neuro.addToQueue(new Patient("Jose Ramirez", 65, "Dementia", Patient.Severity.MODERATE, LocalDateTime.now()));
        neuro.addToQueue(new Patient("Lucia Gonzales", 29, "Nerve Pain", Patient.Severity.MILD, LocalDateTime.now()));
        neuro.addToQueue(new Patient("Sofia Cruz", 44, "Brain Tumor", Patient.Severity.CRITICAL, LocalDateTime.now()));
        neuro.addToQueue(new Patient("Daniel Tan", 39, "Epilepsy", Patient.Severity.SEVERE, LocalDateTime.now()));
        neuro.addToQueue(new Patient("Carmen Reyes", 48, "Parkinson's", Patient.Severity.MODERATE, LocalDateTime.now()));
        neuro.addToQueue(new Patient("Gabriel Ong", 33, "Concussion", Patient.Severity.MILD, LocalDateTime.now()));

        // Cardiology
        Department cardio = departments.get("Cardiology");
        cardio.addToQueue(new Patient("Juan Dela Cruz", 58, "Heart Attack", Patient.Severity.CRITICAL, LocalDateTime.now()));
        cardio.addToQueue(new Patient("Maria Lopez", 62, "Arrhythmia", Patient.Severity.SEVERE, LocalDateTime.now()));
        cardio.addToQueue(new Patient("Pedro Reyes", 47, "High BP", Patient.Severity.MODERATE, LocalDateTime.now()));
        cardio.addToQueue(new Patient("Andrea Sy", 55, "Chest Pain", Patient.Severity.SEVERE, LocalDateTime.now()));
        cardio.addToQueue(new Patient("Rafael Cruz", 63, "Heart Failure", Patient.Severity.CRITICAL, LocalDateTime.now()));
        cardio.addToQueue(new Patient("Isabel Uy", 70, "Valve Disease", Patient.Severity.SEVERE, LocalDateTime.now()));
        cardio.addToQueue(new Patient("Oscar Lim", 50, "Angina", Patient.Severity.MODERATE, LocalDateTime.now()));
        cardio.addToQueue(new Patient("Teresa Santos", 64, "Hypertension", Patient.Severity.MODERATE, LocalDateTime.now()));
        cardio.addToQueue(new Patient("Victor Gonzales", 59, "Palpitations", Patient.Severity.MILD, LocalDateTime.now()));
        cardio.addToQueue(new Patient("Patricia Tan", 45, "Cardiomyopathy", Patient.Severity.SEVERE, LocalDateTime.now()));

        // Orthopedics
        Department ortho = departments.get("Orthopedics");
        ortho.addToQueue(new Patient("Manuel Cruz", 40, "Fracture", Patient.Severity.SEVERE, LocalDateTime.now()));
        ortho.addToQueue(new Patient("Carla Reyes", 35, "Sprain", Patient.Severity.MILD, LocalDateTime.now()));
        ortho.addToQueue(new Patient("Diego Santos", 28, "Dislocation", Patient.Severity.MODERATE, LocalDateTime.now()));
        ortho.addToQueue(new Patient("Elena Lim", 50, "Arthritis", Patient.Severity.MODERATE, LocalDateTime.now()));
        ortho.addToQueue(new Patient("Hector Sy", 31, "Knee Injury", Patient.Severity.SEVERE, LocalDateTime.now()));
        ortho.addToQueue(new Patient("Paula Ong", 45, "Back Injury", Patient.Severity.MODERATE, LocalDateTime.now()));
        ortho.addToQueue(new Patient("Francisco Uy", 60, "Hip Replacement", Patient.Severity.SEVERE, LocalDateTime.now()));
        ortho.addToQueue(new Patient("Diana Gonzales", 29, "Shoulder Pain", Patient.Severity.MILD, LocalDateTime.now()));
        ortho.addToQueue(new Patient("Adrian Medina", 33, "Bone Infection", Patient.Severity.SEVERE, LocalDateTime.now()));
        ortho.addToQueue(new Patient("Rosa Villanueva", 52, "Osteoporosis", Patient.Severity.MODERATE, LocalDateTime.now()));

        // OB-Gyne
        Department obgyn = departments.get("OB-Gynecology");
        obgyn.addToQueue(new Patient("Maria Dela Cruz", 30, "Prenatal Check", Patient.Severity.MILD, LocalDateTime.now()));
        obgyn.addToQueue(new Patient("Angela Reyes", 27, "Labor Pain", Patient.Severity.CRITICAL, LocalDateTime.now()));
        obgyn.addToQueue(new Patient("Samantha Uy", 35, "Miscarriage", Patient.Severity.SEVERE, LocalDateTime.now()));
        obgyn.addToQueue(new Patient("Camille Ong", 29, "Fertility Consult", Patient.Severity.MODERATE, LocalDateTime.now()));
        obgyn.addToQueue(new Patient("Louise Lim", 32, "C-section", Patient.Severity.CRITICAL, LocalDateTime.now()));
        obgyn.addToQueue(new Patient("Julia Gonzales", 26, "Ovarian Cyst", Patient.Severity.SEVERE, LocalDateTime.now()));
        obgyn.addToQueue(new Patient("Catherine Tan", 28, "Pregnancy Check", Patient.Severity.MILD, LocalDateTime.now()));
        obgyn.addToQueue(new Patient("Isabella Sy", 34, "Pelvic Pain", Patient.Severity.MODERATE, LocalDateTime.now()));
        obgyn.addToQueue(new Patient("Nicole Medina", 31, "Birth Control Consult", Patient.Severity.MILD, LocalDateTime.now()));
        obgyn.addToQueue(new Patient("Hannah Cruz", 36, "Menstrual Disorder", Patient.Severity.MODERATE, LocalDateTime.now()));
    }

    private void addDefaultTriagePatients() {
        triageQueue.addPatient(new Patient("ER Patient 1", 22, "Headache", Patient.Severity.MILD, LocalDateTime.now()));
        triageQueue.addPatient(new Patient("ER Patient 2", 30, "Car Accident", Patient.Severity.CRITICAL, LocalDateTime.now()));
        triageQueue.addPatient(new Patient("ER Patient 3", 45, "Flu", Patient.Severity.MODERATE, LocalDateTime.now()));
        triageQueue.addPatient(new Patient("ER Patient 4", 10, "Broken Arm", Patient.Severity.SEVERE, LocalDateTime.now()));
        triageQueue.addPatient(new Patient("ER Patient 5", 67, "Chest Pain", Patient.Severity.CRITICAL, LocalDateTime.now()));
        triageQueue.addPatient(new Patient("ER Patient 6", 55, "Dizziness", Patient.Severity.MODERATE, LocalDateTime.now()));
        triageQueue.addPatient(new Patient("ER Patient 7", 34, "Fever", Patient.Severity.MILD, LocalDateTime.now()));
        triageQueue.addPatient(new Patient("ER Patient 8", 29, "Pregnancy Pain", Patient.Severity.SEVERE, LocalDateTime.now()));
        triageQueue.addPatient(new Patient("ER Patient 9", 40, "Accident Wound", Patient.Severity.MODERATE, LocalDateTime.now()));
        triageQueue.addPatient(new Patient("ER Patient 10", 15, "Asthma Attack", Patient.Severity.CRITICAL, LocalDateTime.now()));
    }

    public void setLogListener(LogListener listener) {
        this.logListener = listener;
    }

    private void log(String message) {
        if (logListener != null) logListener.onLog(message);
    }

    public void addPatientToER(Patient patient) {
        triageQueue.addPatient(patient);
        log("New patient arrived at ER: " + patient);
    }

    public void moveFromERToDepartment(String deptName) {
        if (!triageQueue.isEmpty()) {
            Patient p = triageQueue.pollPatient();
            departments.get(deptName).addToQueue(p);
            log("Moved from ER to " + deptName + ": " + p);
        }
    }

    public void startTreatment(String deptName) {
        Department dept = departments.get(deptName);
        Patient p = dept.startTreatment();
        if (p != null) log("Started treatment in " + deptName + ": " + p);
    }

    public void releasePatient(String deptName) {
        Department dept = departments.get(deptName);
        Patient p = dept.releasePatient();
        if (p != null) {
            releasedPatients.add(p);
            log("Released from " + deptName + ": " + p);
        }
    }

    public Map<String, Department> getDepartments() { return departments; }
    public TriageQueue getTriageQueue() { return triageQueue; }
    public List<Patient> getReleasedPatients() { return releasedPatients; }

    // 🔹 Fix for GUI error
    public Department getDepartment(String name) {
        return departments.get(name);
    }
}

import java.time.LocalDateTime;

// Simple Patient class with easy-to-understand fields.
// We keep the same API but use friendlier variable names and comments.
public class Patient {
    public enum Severity { VERY_MINOR(1), MILD(2), MODERATE(3), SEVERE(4), CRITICAL(5);
        private final int level;
        Severity(int level) { this.level = level; }
        public int getLevel() { return level; }
    }

    public enum Status { ER, QUEUED, ONGOING, CONFINED, RELEASED }

    // simple id counter for each new patient
    private static int nextId = 1;
    private final int id;

    private String name;
    private int age;
    private String caseDescription;
    private Severity severity;
    private LocalDateTime arrivalTime;
    private LocalDateTime releaseTime;
    private Status status;
    private String department;
    private String assignedDoctor;
    private String confinementInfo;

    // confinement fields
    private int confinementRoom = 0; // 0 means none
    private int confinementDays = 0;
    // Optional clinical/vital fields (may be null if not collected). These help confinement decisions.
    private Integer spO2 = null; // peripheral oxygen saturation percent
    private Integer systolicBP = null; // check blood pressure
    private boolean seizing = false; // ongoing uncontrolled seizure
    private boolean hasMajorComorbidity = false; // smthing like HF, COPD, CKD
    private Double trendScore = 0.0; // >0 would worsen, <0 would be improving, small magnitude

    public Patient(String name, int age, String caseDescription, Severity severity, LocalDateTime arrivalTime) {
        this.id = nextId++;
        if (name == null || name.isBlank()) this.name = "Patient " + id; else this.name = name.trim();
        this.age = Math.max(0, age);
        if (caseDescription == null || caseDescription.isBlank()) this.caseDescription = "General"; else this.caseDescription = caseDescription.trim();
        this.severity = (severity == null) ? Severity.MILD : severity;
        this.arrivalTime = (arrivalTime == null) ? LocalDateTime.now() : arrivalTime;
        this.status = Status.ER;
        this.department = null;
        this.assignedDoctor = null;
        this.confinementInfo = null;
        this.releaseTime = null;
    }

    // simple getters and setters (same API we did before)
    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getCaseDescription() { return caseDescription; }
    public Severity getSeverity() { return severity; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public LocalDateTime getReleaseTime() { return releaseTime; }
    public Status getStatus() { return status; }
    public String getDepartment() { return department; }
    public String getAssignedDoctor() { return assignedDoctor; }
    public String getConfinementInfo() { return confinementInfo; }
    public int getConfinementRoom() { return confinementRoom; }
    public int getConfinementDays() { return confinementDays; }

    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setCaseDescription(String caseDescription) { this.caseDescription = caseDescription; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }
    public void setReleaseTime(LocalDateTime releaseTime) { this.releaseTime = releaseTime; }
    public void setStatus(Status status) { this.status = status; }
    public void setDepartment(String department) { this.department = department; }
    public void setAssignedDoctor(String assignedDoctor) { this.assignedDoctor = assignedDoctor; }
    public void setConfinementInfo(String confinementInfo) { this.confinementInfo = confinementInfo; }
    public void setConfinementRoom(int room) { this.confinementRoom = room; }
    public void setConfinementDays(int days) { this.confinementDays = days; }

    // getters ng vitals (randomzied siy)
    public Integer getSpO2() { return spO2; }
    public void setSpO2(Integer spO2) { this.spO2 = spO2; }
    public Integer getSystolicBP() { return systolicBP; }
    public void setSystolicBP(Integer systolicBP) { this.systolicBP = systolicBP; }
    public boolean isSeizing() { return seizing; }
    public void setSeizing(boolean seizing) { this.seizing = seizing; }
    public boolean hasMajorComorbidity() { return hasMajorComorbidity; }
    public void setHasMajorComorbidity(boolean hasMajorComorbidity) { this.hasMajorComorbidity = hasMajorComorbidity; }
    public Double getTrendScore() { return trendScore; }
    public void setTrendScore(Double trendScore) { this.trendScore = trendScore == null ? 0.0 : trendScore; }

    public boolean isImproving() { return this.trendScore != null && this.trendScore < 0.0; }

    @Override
    public String toString() {
        return String.format("[ID %d] %s (Age %d) - %s - Sev:%s - Dept:%s - Status:%s - Arr:%s",
                id, name, age, caseDescription, severity.name(),
                (department == null ? "ER" : department),
                status.name(), arrivalTime.toString());
    }
}

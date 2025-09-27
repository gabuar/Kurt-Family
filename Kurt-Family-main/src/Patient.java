import java.time.LocalDateTime;

/**
 * Patient model used by the hospital system.
 * Added confinement room/day fields to support room occupancy tracking.
 */
public class Patient {
    public enum Severity { VERY_MINOR(1), MILD(2), MODERATE(3), SEVERE(4), CRITICAL(5);
        private final int level;
        Severity(int level) { this.level = level; }
        public int getLevel() { return level; }
    }

    public enum Status { ER, QUEUED, ONGOING, CONFINED, RELEASED }

    private static int COUNTER = 1;
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

    // new fields
    private int confinementRoom = 0; // 0 == none
    private int confinementDays = 0;

    public Patient(String name, int age, String caseDescription, Severity severity, LocalDateTime arrivalTime) {
        this.id = COUNTER++;
        this.name = (name == null || name.isBlank()) ? ("Patient " + id) : name.trim();
        this.age = Math.max(0, age);
        this.caseDescription = (caseDescription == null || caseDescription.isBlank()) ? "General" : caseDescription.trim();
        this.severity = (severity == null) ? Severity.MILD : severity;
        this.arrivalTime = (arrivalTime == null) ? LocalDateTime.now() : arrivalTime;
        this.status = Status.ER;
        this.department = null;
        this.assignedDoctor = null;
        this.confinementInfo = null;
        this.releaseTime = null;
    }

    // getters & setters
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

    @Override
    public String toString() {
        return String.format("[ID %d] %s (Age %d) - %s - Sev:%s - Dept:%s - Status:%s - Arr:%s",
                id, name, age, caseDescription, severity.name(),
                (department == null ? "ER" : department),
                status.name(), arrivalTime.toString());
    }
}

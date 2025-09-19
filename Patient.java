import java.time.*;

public class Patient {
    public enum Severity {
        VERY_MINOR(1),
        MILD(2),
        MODERATE(3),
        SEVERE(4),
        CRITICAL(5);

        private final int level;

        Severity(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    private String name;
    private int age;
    private String caseDescription;
    private Severity severity;
    private LocalDateTime arrivalTime;

    public Patient(String name, int age, String caseDescription, Severity severity, LocalDateTime arrivalTime) {
        this.name = name;
        this.age = age;
        this.caseDescription = caseDescription;
        this.severity = severity;
        this.arrivalTime = (arrivalTime != null) ? arrivalTime : LocalDateTime.now();
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public String getCaseDescription() { return caseDescription; }
    public Severity getSeverity() { return severity; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }

    @Override
    public String toString() {
        return String.format("%s (Age %d, Severity %d - %s, Case: %s, Arrival: %s)",
                name,
                age,
                severity.getLevel(),
                severity.name(),
                caseDescription,
                arrivalTime.toString());
    }
}

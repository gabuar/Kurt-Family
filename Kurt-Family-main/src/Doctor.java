/**
 * Doctor model: each doctor belongs to a department and has availability.
 */
public class Doctor {
    private static int COUNTER = 1;

    private final int id;
    private final String name;
    private final String department;
    private boolean available;

    public Doctor(String name, String department) {
        this.id = COUNTER++;
        this.name = (name == null || name.isBlank()) ? ("Doctor " + id) : name.trim();
        this.department = department;
        this.available = true;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return String.format("Dr. %s - %s", name, (available ? "Open Slot" : "Busy"));
    }
}

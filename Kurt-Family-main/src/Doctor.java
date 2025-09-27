// Simple Doctor class. Easy to read for students.
// Each Doctor has an id, a name, a department string, and a flag if they are available.
public class Doctor {
    // simple id generator for doctors
    private static int nextId = 1;

    private final int id;
    private final String name;
    private final String department;
    private boolean available;

    // constructor: give a name and department. If name is empty, use "Doctor <id>".
    public Doctor(String name, String department) {
        this.id = nextId++;
        if (name == null || name.isBlank()) {
            this.name = "Doctor " + id;
        } else {
            this.name = name.trim();
        }
        this.department = department;
        this.available = true; // doctors start as available by default
    }

    // getters and a setter for availability. Keep these simple.
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    // Nice string for debugging or showing in lists
    @Override
    public String toString() {
        return String.format("Dr. %s - %s", name, (available ? "Open Slot" : "Busy"));
    }
}

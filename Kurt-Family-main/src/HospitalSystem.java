import java.time.LocalDateTime;
import java.util.*;

/**
 * Core hospital system:
 * - triage queue
 * - departments
 * - doctor pools
 * - released & confined records
 * - room occupancy tracking (rooms 1..101)
 *
 * Confine method now accepts room number and days and ensures rooms cannot be double-booked.
 */
public class HospitalSystem {
    private final Map<String, Department> departments = new LinkedHashMap<>();
    private final TriageQueue triageQueue = new TriageQueue();
    private final Map<String, List<Doctor>> doctorPools = new HashMap<>();
    private final List<Patient> releasedPatients = new ArrayList<>();
    private final Map<Integer, Patient> occupiedRooms = new HashMap<>(); // roomNumber -> patient
    private LogListener logListener;

    private static final String[] GREYS_DOCTORS = {
        "Meredith Grey", "Derek Shepherd", "Cristina Yang", "Miranda Bailey", "Alex Karev",
        "Arizona Robbins", "Richard Webber", "Callie Torres", "Izzie Stevens", "George O'Malley",
        "Amelia Shepherd", "Owen Hunt", "Jackson Avery", "Stephanie Edwards", "Leah Murphy"
    };

    public HospitalSystem() {
        String[] deptNames = {"General / Clinic", "Pediatrics", "Neurology", "Cardiology", "Orthopedics", "OB-Gynecology"};
        for (String n : deptNames) {
            departments.put(n, new Department(n));
            doctorPools.put(n, new ArrayList<>());
        }

        // distribute doctors
        int di = 0;
        List<String> docNames = new ArrayList<>(Arrays.asList(GREYS_DOCTORS));
        for (String dept : departments.keySet()) {
            List<Doctor> pool = doctorPools.get(dept);
            for (int i = 0; i < 5; i++) {
                String dn = docNames.get(di % docNames.size());
                pool.add(new Doctor(dn, dept));
                di++;
            }
        }

        seedInitialData();
        autoFillAllDepartments();
    }

    public void setLogListener(LogListener l) { this.logListener = l; }
    private void log(String s) { if (logListener != null) logListener.onLog("[" + LocalDateTime.now().toString() + "] " + s); }

    // ER
    public void addPatientToER(Patient p) {
        if (p.getArrivalTime() == null) p.setArrivalTime(LocalDateTime.now());
        p.setStatus(Patient.Status.ER);
        triageQueue.addPatient(p);
        log("ER: New patient -> " + p.getName() + " (ID " + p.getId() + ")");
    }

    public Patient findPatientInTriageById(int id) {
        for (Patient p : triageQueue.getQueue()) if (p.getId() == id) return p;
        return null;
    }

    public boolean movePatientToDepartmentById(int patientId, String deptName) {
        Patient p = findPatientInTriageById(patientId);
        if (p == null) return false;
        boolean removed = triageQueue.getQueue().remove(p);
        if (!removed) return false;
        Department dept = departments.get(deptName);
        if (dept == null) {
            triageQueue.addPatient(p);
            return false;
        }
        dept.addToQueue(p);
        log("Moved to " + deptName + ": " + p.getName() + " (ID " + p.getId() + ")");
        autoFillDepartment(deptName);
        return true;
    }

    // Doctor management
    public List<Doctor> getDoctorsForDept(String deptName) {
        return doctorPools.getOrDefault(deptName, new ArrayList<>());
    }

    public void addDoctorToDept(String deptName, String doctorName) {
        Doctor d = new Doctor(doctorName, deptName);
        doctorPools.computeIfAbsent(deptName, k -> new ArrayList<>()).add(d);
        log("Added doctor " + d.getName() + " to " + deptName);
        autoFillDepartment(deptName);
    }

    public void toggleDoctorAvailability(String deptName, int doctorId, boolean available) {
        List<Doctor> pool = doctorPools.get(deptName);
        if (pool == null) return;
        for (Doctor d : pool) {
            if (d.getId() == doctorId) {
                d.setAvailable(available);
                log("Doctor " + d.getName() + " availability -> " + available);
                break;
            }
        }
        autoFillDepartment(deptName);
    }

    // Auto assign available doctors to queued patients
    public void autoFillDepartment(String deptName) {
        Department dept = departments.get(deptName);
        if (dept == null) return;

        List<Doctor> pool = doctorPools.getOrDefault(deptName, Collections.emptyList());
        List<Doctor> available = new ArrayList<>();
        for (Doctor d : pool) if (d.isAvailable()) available.add(d);

        int allowedSlots = Math.min(available.size(), Department.MAX_ONGOING - dept.ongoingCount());
        Iterator<Doctor> docIt = available.iterator();
        while (allowedSlots > 0 && dept.peekNextWaiting() != null && docIt.hasNext()) {
            Doctor doc = docIt.next();
            Patient next = dept.pollNextWaiting();
            if (next == null) break;
            boolean ok = dept.addToOngoing(next, doc);
            if (ok) {
                doc.setAvailable(false);
                log("Assigned " + next.getName() + " (ID " + next.getId() + ") -> " + doc.getName() + " in " + deptName);
                allowedSlots--;
            } else {
                dept.addToQueue(next);
                break;
            }
        }
    }

    public void autoFillAllDepartments() {
        for (String name : departments.keySet()) autoFillDepartment(name);
    }

    // Release ongoing: frees that specific doctor and records release
    public boolean releaseOngoingPatient(String deptName, int patientId) {
        Department dept = departments.get(deptName);
        if (dept == null) return false;
        Department.OngoingEntry entry = dept.popOngoingEntryByPatientId(patientId);
        if (entry == null) return false;

        Patient p = entry.getPatient();
        Doctor d = entry.getDoctor();

        p.setStatus(Patient.Status.RELEASED);
        p.setReleaseTime(LocalDateTime.now());
        p.setAssignedDoctor(null);
        releasedPatients.add(p);

        d.setAvailable(true);
        log("Released " + p.getName() + " (ID " + p.getId() + ") from " + deptName + " (Doctor: " + d.getName() + ")");
        autoFillDepartment(deptName);
        return true;
    }

    // Confine ongoing: requires a free room; stores room/day in patient and marks room occupied
    public boolean confineOngoingPatient(String deptName, int patientId, int roomNumber, int days) {
        if (roomNumber < 1 || roomNumber > 101) return false;
        if (isRoomOccupied(roomNumber)) return false;

        Department dept = departments.get(deptName);
        if (dept == null) return false;
        Department.OngoingEntry entry = dept.popOngoingEntryByPatientId(patientId);
        if (entry == null) return false;

        Patient p = entry.getPatient();
        Doctor d = entry.getDoctor();

        p.setConfinementRoom(roomNumber);
        p.setConfinementDays(days);
        String info = "Room " + roomNumber + " - " + days + " day(s)";
        p.setConfinementInfo(info);
        p.setStatus(Patient.Status.CONFINED);
        p.setAssignedDoctor(null);
        dept.addConfined(p);

        // mark room occupied
        occupyRoom(roomNumber, p);

        // free doctor
        d.setAvailable(true);
        log("Confined " + p.getName() + " (ID " + p.getId() + ") in " + deptName + " (" + info + ") - freed doctor " + d.getName());
        autoFillDepartment(deptName);
        return true;
    }

    // Release confined patient -> free room and move to releasedRecords
    public boolean releaseConfinedPatient(String deptName, int patientId) {
        Department dept = departments.get(deptName);
        if (dept == null) return false;
        Patient p = dept.releaseConfinedByPatientId(patientId);
        if (p == null) return false;
        // free room if set
        if (p.getConfinementRoom() > 0) freeRoom(p.getConfinementRoom());
        p.setReleaseTime(LocalDateTime.now());
        releasedPatients.add(p);
        log("Released confined patient " + p.getName() + " (ID " + p.getId() + ") from " + deptName);
        return true;
    }

    // Room management
    public boolean isRoomOccupied(int room) { return occupiedRooms.containsKey(room); }
    public void occupyRoom(int room, Patient p) { if (room >= 1 && room <= 101) occupiedRooms.put(room, p); }
    public void freeRoom(int room) { occupiedRooms.remove(room); }
    public Map<Integer, Patient> getOccupiedRoomsSnapshot() { return new HashMap<>(occupiedRooms); }

    // Records / search / export / stats
    public List<Patient> getReleasedPatients() { return Collections.unmodifiableList(releasedPatients); }
    public Map<String, Department> getDepartments() { return Collections.unmodifiableMap(departments); }
    public TriageQueue getTriageQueue() { return triageQueue; }

    public List<Patient> searchReleasedPatients(String query) {
        String q = query == null ? "" : query.toLowerCase().trim();
        List<Patient> out = new ArrayList<>();
        for (Patient p : releasedPatients) {
            if (String.valueOf(p.getId()).equals(q) || p.getName().toLowerCase().contains(q)) out.add(p);
        }
        return out;
    }

    public String exportReleasedPatientsToCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Name,Age,Case,Department,Status,Arrived,Released,ConfinementInfo\n");
        for (Patient p : releasedPatients) {
            sb.append(String.format("%d,%s,%d,%s,%s,%s,%s,%s,%s\n",
                    p.getId(),
                    escapeCsv(p.getName()),
                    p.getAge(),
                    escapeCsv(p.getCaseDescription()),
                    p.getDepartment() == null ? "" : escapeCsv(p.getDepartment()),
                    p.getStatus().name(),
                    p.getArrivalTime() == null ? "" : p.getArrivalTime().toString(),
                    p.getReleaseTime() == null ? "" : p.getReleaseTime().toString(),
                    p.getConfinementInfo() == null ? "" : escapeCsv(p.getConfinementInfo())
            ));
        }
        return sb.toString();
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    public Map<String, Integer> getStats() {
        Map<String, Integer> m = new LinkedHashMap<>();
        int inER = triageQueue.getQueue().size();
        int queued = 0, ongoing = 0, confined = 0, released = releasedPatients.size();
        for (Department d : departments.values()) {
            queued += d.waitingCount();
            ongoing += d.ongoingCount();
            confined += d.getConfinedSnapshot().size();
        }
        m.put("In ER", inER);
        m.put("Queued (all depts)", queued);
        m.put("Ongoing (all depts)", ongoing);
        m.put("Confined (all depts)", confined);
        m.put("Released", released);
        return m;
    }

    private void seedInitialData() {
        addPatientToER(new Patient("Tony Stark", 48, "Chest Pain", Patient.Severity.CRITICAL, LocalDateTime.now()));
        addPatientToER(new Patient("Ellen Ripley", 34, "Burns", Patient.Severity.SEVERE, LocalDateTime.now()));
        Department gen = departments.get("General / Clinic");
        if (gen != null) gen.addToQueue(new Patient("Sarah Connor", 37, "Fever", Patient.Severity.MILD, LocalDateTime.now()));
        Department cardio = departments.get("Cardiology");
        if (cardio != null) cardio.addToQueue(new Patient("Jack Dawson", 28, "Palpitations", Patient.Severity.MODERATE, LocalDateTime.now()));
    }
}

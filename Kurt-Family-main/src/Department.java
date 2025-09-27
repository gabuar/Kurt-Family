import java.time.LocalDateTime;
import java.util.*;

// Department manages its own waiting queue, ongoing slots, and confined patients.
// The waiting queue uses severity (higher first) and then arrival time.
public class Department {
    public static final int MAX_ONGOING = 5;

    // small helper class: when a doctor treats a patient we store who and when it started
    public static class OngoingEntry {
        private final Patient patient;
        private final Doctor doctor;
        private final LocalDateTime start;

        public OngoingEntry(Patient patient, Doctor doctor, LocalDateTime start) {
            this.patient = patient;
            this.doctor = doctor;
            this.start = start;
        }

        public Patient getPatient() { return patient; }
        public Doctor getDoctor() { return doctor; }
        public LocalDateTime getStart() { return start; }
    }

    private final String name;
    private final PriorityQueue<Patient> waitingQueue;
    private final List<OngoingEntry> ongoing;
    private final List<Patient> confined;

    public Department(String name) {
        this.name = name;
        // comparator: severity desc (bigger number = more severe), then earlier arrival first
        this.waitingQueue = new PriorityQueue<>(
            Comparator.comparingInt((Patient p) -> p.getSeverity().getLevel()).reversed()
                      .thenComparing(Patient::getArrivalTime)
        );
        this.ongoing = new ArrayList<>();
        this.confined = new ArrayList<>();
    }

    public String getName() { return name; }

    // add patient to waiting queue and mark their department/status
    public synchronized void addToQueue(Patient p) {
        if (p == null) return;
        p.setDepartment(name);
        p.setStatus(Patient.Status.QUEUED);
        waitingQueue.offer(p);
    }

    public synchronized Patient peekNextWaiting() { return waitingQueue.peek(); }
    public synchronized Patient pollNextWaiting() { return waitingQueue.poll(); }

    // put a patient into an ongoing slot with a doctor
    public synchronized boolean addToOngoing(Patient p, Doctor d) {
        if (p == null || d == null) return false;
        if (ongoing.size() >= MAX_ONGOING) return false;
        p.setStatus(Patient.Status.ONGOING);
        p.setAssignedDoctor(d.getName());
        ongoing.add(new OngoingEntry(p, d, LocalDateTime.now()));
        return true;
    }

    // remove an ongoing entry by patient id and return it
    public synchronized OngoingEntry popOngoingEntryByPatientId(int patientId) {
        Iterator<OngoingEntry> it = ongoing.iterator();
        while (it.hasNext()) {
            OngoingEntry e = it.next();
            if (e.getPatient().getId() == patientId) {
                it.remove();
                return e;
            }
        }
        return null;
    }

    // add a confined patient (they are removed from ongoing first by the caller)
    public synchronized void addConfined(Patient p) {
        if (p == null) return;
        p.setStatus(Patient.Status.CONFINED);
        p.setAssignedDoctor(null);
        confined.add(p);
    }

    // release a confined patient and return that patient object
    public synchronized Patient releaseConfinedByPatientId(int patientId) {
        Iterator<Patient> it = confined.iterator();
        while (it.hasNext()) {
            Patient p = it.next();
            if (p.getId() == patientId) {
                it.remove();
                p.setStatus(Patient.Status.RELEASED);
                return p;
            }
        }
        return null;
    }

    // return snapshots for UI (copies so callers don't modify internal lists)
    public synchronized List<Patient> getWaitingSnapshotSorted() {
        PriorityQueue<Patient> copy = new PriorityQueue<>(waitingQueue);
        List<Patient> out = new ArrayList<>();
        while (!copy.isEmpty()) out.add(copy.poll());
        return out;
    }
    public synchronized List<OngoingEntry> getOngoingSnapshot() { return new ArrayList<>(ongoing); }
    public synchronized List<Patient> getConfinedSnapshot() { return new ArrayList<>(confined); }

    public synchronized int waitingCount() { return waitingQueue.size(); }
    public synchronized int ongoingCount() { return ongoing.size(); }
}

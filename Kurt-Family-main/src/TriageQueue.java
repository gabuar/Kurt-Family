import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * ER triage queue (priority: severity desc, arrival asc).
 */
public class TriageQueue {
    private final PriorityQueue<Patient> queue;

    public TriageQueue() {
        queue = new PriorityQueue<>(Comparator
            .comparingInt((Patient p) -> p.getSeverity().getLevel())
            .reversed()
            .thenComparing(Patient::getArrivalTime));
    }

    public void addPatient(Patient p) {
        if (p == null) return;
        if (p.getArrivalTime() == null) p.setArrivalTime(LocalDateTime.now());
        p.setStatus(Patient.Status.ER);
        queue.offer(p);
    }

    public Patient pollPatient() { return queue.poll(); }
    public boolean isEmpty() { return queue.isEmpty(); }
    public PriorityQueue<Patient> getQueue() { return queue; }
}

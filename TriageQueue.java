import java.util.Comparator;
import java.util.PriorityQueue;

public class TriageQueue {
    private PriorityQueue<Patient> queue;

    public TriageQueue() {
        queue = new PriorityQueue<>(Comparator
                .comparingInt((Patient p) -> p.getSeverity().getLevel())
                .reversed()
                .thenComparing(Patient::getArrivalTime));
    }

    public void addPatient(Patient patient) {
        queue.offer(patient);
    }

    public Patient pollPatient() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public PriorityQueue<Patient> getQueue() {
        return queue;
    }
}

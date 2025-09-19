import java.util.Comparator;
import java.util.PriorityQueue;

public class TriageQueue {
    private PriorityQueue<Patient> queue;

    public TriageQueue() {
        // Higher severity first, then earlier arrival
        queue = new PriorityQueue<>(Comparator
                .comparingInt((Patient p) -> -p.getSeverity().getLevel())
                .thenComparing(Patient::getArrivalTime));
    }

    public void addPatient(Patient patient) {
        queue.offer(patient);
    }

    public Patient nextPatient() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }

    public PriorityQueue<Patient> getQueue() {
        return queue;
    }

    @Override
    public String toString() {
        return queue.toString();
    }
}

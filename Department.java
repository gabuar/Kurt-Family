import java.util.LinkedList;
import java.util.Queue;

public class Department {
    private String name;
    private Queue<Patient> waitingQueue;
    private Queue<Patient> ongoing;

    public Department(String name) {
        this.name = name;
        this.waitingQueue = new LinkedList<>();
        this.ongoing = new LinkedList<>();
    }

    public void addToQueue(Patient patient) {
        waitingQueue.offer(patient);
    }

    public Patient startTreatment() {
        if (!waitingQueue.isEmpty()) {
            Patient patient = waitingQueue.poll();
            ongoing.offer(patient);
            return patient;
        }
        return null;
    }

    public Patient releasePatient() {
        if (!ongoing.isEmpty()) {
            return ongoing.poll();
        }
        return null;
    }

    public String getName() { return name; }
    public Queue<Patient> getWaitingQueue() { return waitingQueue; }
    public Queue<Patient> getOngoingQueue() { return ongoing; }
}

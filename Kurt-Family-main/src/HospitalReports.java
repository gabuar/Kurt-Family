import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Hospital Reports System with Quick Sort implementation
 * Generates various sorted reports for hospital management
 */
public class HospitalReports {
    private final HospitalSystem system;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public HospitalReports(HospitalSystem system) {
        this.system = system;
    }
    
    // ==================== QUICK SORT IMPLEMENTATIONS ====================
    
    /**
     * Quick Sort for Patient lists with different sorting criteria
     */
    public static void quickSortPatients(List<Patient> patients, String sortBy) {
        if (patients == null || patients.size() <= 1) return;
        quickSortPatientsRecursive(patients, 0, patients.size() - 1, sortBy);
    }
    
    private static void quickSortPatientsRecursive(List<Patient> list, int low, int high, String sortBy) {
        if (low < high) {
            int pivotIndex = partitionPatients(list, low, high, sortBy);
            quickSortPatientsRecursive(list, low, pivotIndex - 1, sortBy);
            quickSortPatientsRecursive(list, pivotIndex + 1, high, sortBy);
        }
    }
    
    private static int partitionPatients(List<Patient> list, int low, int high, String sortBy) {
        Patient pivot = list.get(high);
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            if (comparePatients(list.get(j), pivot, sortBy) <= 0) {
                i++;
                Collections.swap(list, i, j);
            }
        }
        Collections.swap(list, i + 1, high);
        return i + 1;
    }
    
    private static int comparePatients(Patient p1, Patient p2, String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "id":
                return Integer.compare(p1.getId(), p2.getId());
            case "name":
                return p1.getName().compareToIgnoreCase(p2.getName());
            case "age":
                return Integer.compare(p1.getAge(), p2.getAge());
            case "severity":
                return Integer.compare(p2.getSeverity().getLevel(), p1.getSeverity().getLevel()); // Descending
            case "arrival":
                if (p1.getArrivalTime() == null && p2.getArrivalTime() == null) return 0;
                if (p1.getArrivalTime() == null) return 1;
                if (p2.getArrivalTime() == null) return -1;
                return p1.getArrivalTime().compareTo(p2.getArrivalTime());
            case "release":
                if (p1.getReleaseTime() == null && p2.getReleaseTime() == null) return 0;
                if (p1.getReleaseTime() == null) return 1;
                if (p2.getReleaseTime() == null) return -1;
                return p1.getReleaseTime().compareTo(p2.getReleaseTime());
            default:
                return p1.getName().compareToIgnoreCase(p2.getName());
        }
    }
    
    /**
     * Quick Sort for Department Statistics
     */
    public static void quickSortDepartmentStats(List<DepartmentReport> reports, String sortBy) {
        if (reports == null || reports.size() <= 1) return;
        quickSortDeptStatsRecursive(reports, 0, reports.size() - 1, sortBy);
    }
    
    private static void quickSortDeptStatsRecursive(List<DepartmentReport> list, int low, int high, String sortBy) {
        if (low < high) {
            int pivotIndex = partitionDeptStats(list, low, high, sortBy);
            quickSortDeptStatsRecursive(list, low, pivotIndex - 1, sortBy);
            quickSortDeptStatsRecursive(list, pivotIndex + 1, high, sortBy);
        }
    }
    
    private static int partitionDeptStats(List<DepartmentReport> list, int low, int high, String sortBy) {
        DepartmentReport pivot = list.get(high);
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            if (compareDeptReports(list.get(j), pivot, sortBy) <= 0) {
                i++;
                Collections.swap(list, i, j);
            }
        }
        Collections.swap(list, i + 1, high);
        return i + 1;
    }
    
    private static int compareDeptReports(DepartmentReport r1, DepartmentReport r2, String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "name":
                return r1.departmentName.compareToIgnoreCase(r2.departmentName);
            case "waiting":
                return Integer.compare(r2.waitingCount, r1.waitingCount); // Descending
            case "ongoing":
                return Integer.compare(r2.ongoingCount, r1.ongoingCount); // Descending
            case "confined":
                return Integer.compare(r2.confinedCount, r1.confinedCount); // Descending
            case "total":
                return Integer.compare(r2.getTotalPatients(), r1.getTotalPatients()); // Descending
            default:
                return r1.departmentName.compareToIgnoreCase(r2.departmentName);
        }
    }
    
    // ==================== REPORT DATA CLASSES ====================
    
    public static class DepartmentReport {
        public String departmentName;
        public int waitingCount;
        public int ongoingCount;
        public int confinedCount;
        public int doctorsCount;
        public int availableDoctors;
        
        public int getTotalPatients() {
            return waitingCount + ongoingCount + confinedCount;
        }
        
        @Override
        public String toString() {
            return String.format("%-20s | Wait: %3d | Ongoing: %3d | Confined: %3d | Doctors: %d/%d", 
                departmentName, waitingCount, ongoingCount, confinedCount, availableDoctors, doctorsCount);
        }
    }
    
    public static class DoctorReport {
        public String doctorName;
        public String department;
        public boolean isAvailable;
        public String currentPatient;
        
        @Override
        public String toString() {
            return String.format("%-20s | %-15s | %s | %s", 
                doctorName, department, 
                isAvailable ? "Available" : "Busy", 
                currentPatient != null ? currentPatient : "No patient");
        }
    }
    
    // ==================== REPORT GENERATION METHODS ====================
    
    /**
     * Generate Department Summary Report (sorted by Quick Sort)
     */
    public String generateDepartmentReport(String sortBy) {
        List<DepartmentReport> reports = new ArrayList<>();
        
        for (Map.Entry<String, Department> entry : system.getDepartments().entrySet()) {
            String deptName = entry.getKey();
            Department dept = entry.getValue();
            List<Doctor> doctors = system.getDoctorsForDept(deptName);
            
            DepartmentReport report = new DepartmentReport();
            report.departmentName = deptName;
            report.waitingCount = dept.waitingCount();
            report.ongoingCount = dept.ongoingCount();
            report.confinedCount = dept.getConfinedSnapshot().size();
            report.doctorsCount = doctors.size();
            report.availableDoctors = (int) doctors.stream().filter(Doctor::isAvailable).count();
            
            reports.add(report);
        }
        
        // Apply Quick Sort
        quickSortDepartmentStats(reports, sortBy);
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== DEPARTMENT REPORT (Sorted by: ").append(sortBy.toUpperCase()).append(") ===\n");
        sb.append("Generated: ").append(LocalDateTime.now().format(dtf)).append("\n\n");
        sb.append(String.format("%-20s | %-8s | %-10s | %-12s | %s\n", 
            "Department", "Waiting", "Ongoing", "Confined", "Doctors (Avail/Total)"));
        sb.append("-".repeat(80)).append("\n");
        
        for (DepartmentReport report : reports) {
            sb.append(report.toString()).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Generate Patient Report (sorted by Quick Sort)
     */
    public String generatePatientReport(String patientType, String sortBy) {
        List<Patient> patients = new ArrayList<>();
        
        switch (patientType.toLowerCase()) {
            case "released":
                patients.addAll(system.getReleasedPatients());
                break;
            case "er":
                patients.addAll(system.getTriageQueue().getQueue());
                break;
            case "all":
                patients.addAll(system.getReleasedPatients());
                patients.addAll(system.getTriageQueue().getQueue());
                for (Department dept : system.getDepartments().values()) {
                    patients.addAll(dept.getWaitingSnapshotSorted());
                    for (Department.OngoingEntry entry : dept.getOngoingSnapshot()) {
                        patients.add(entry.getPatient());
                    }
                    patients.addAll(dept.getConfinedSnapshot());
                }
                break;
        }
        
        // Apply Quick Sort
        quickSortPatients(patients, sortBy);
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== PATIENT REPORT (").append(patientType.toUpperCase()).append(" - Sorted by: ").append(sortBy.toUpperCase()).append(") ===\n");
        sb.append("Generated: ").append(LocalDateTime.now().format(dtf)).append("\n\n");
        sb.append(String.format("%-4s | %-20s | %-4s | %-15s | %-10s | %-15s | %s\n", 
            "ID", "Name", "Age", "Case", "Severity", "Department", "Status"));
        sb.append("-".repeat(100)).append("\n");
        
        for (Patient p : patients) {
            sb.append(String.format("%-4d | %-20s | %-4d | %-15s | %-10s | %-15s | %s\n",
                p.getId(),
                p.getName().length() > 20 ? p.getName().substring(0, 17) + "..." : p.getName(),
                p.getAge(),
                p.getCaseDescription().length() > 15 ? p.getCaseDescription().substring(0, 12) + "..." : p.getCaseDescription(),
                p.getSeverity().name(),
                p.getDepartment() != null ? p.getDepartment() : "ER",
                p.getStatus().name()
            ));
        }
        
        return sb.toString();
    }
    
    /**
     * Generate Room Occupancy Report
     */
    public String generateRoomReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ROOM OCCUPANCY REPORT ===\n");
        sb.append("Generated: ").append(LocalDateTime.now().format(dtf)).append("\n\n");
        
        Map<Integer, Patient> occupiedRooms = system.getOccupiedRoomsSnapshot();
        List<Integer> roomNumbers = new ArrayList<>(occupiedRooms.keySet());
        
        // Quick sort room numbers
        Collections.sort(roomNumbers); // Using built-in sort for simplicity with integers
        
        sb.append("Occupied Rooms: ").append(occupiedRooms.size()).append("/101\n");
        sb.append("Available Rooms: ").append(101 - occupiedRooms.size()).append("/101\n\n");
        
        if (!roomNumbers.isEmpty()) {
            sb.append(String.format("%-6s | %-20s | %-4s | %-15s | %s\n", 
                "Room", "Patient Name", "Age", "Case", "Days"));
            sb.append("-".repeat(70)).append("\n");
            
            for (Integer roomNum : roomNumbers) {
                Patient p = occupiedRooms.get(roomNum);
                sb.append(String.format("%-6d | %-20s | %-4d | %-15s | %d\n",
                    roomNum,
                    p.getName().length() > 20 ? p.getName().substring(0, 17) + "..." : p.getName(),
                    p.getAge(),
                    p.getCaseDescription().length() > 15 ? p.getCaseDescription().substring(0, 12) + "..." : p.getCaseDescription(),
                    p.getConfinementDays()
                ));
            }
        } else {
            sb.append("No rooms currently occupied.\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Generate Summary Statistics Report
     */
    public String generateSummaryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== HOSPITAL SUMMARY REPORT ===\n");
        sb.append("Generated: ").append(LocalDateTime.now().format(dtf)).append("\n\n");
        
        Map<String, Integer> stats = system.getStats();
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            sb.append(String.format("%-25s: %d\n", entry.getKey(), entry.getValue()));
        }
        
        sb.append("\n=== DEPARTMENT BREAKDOWN ===\n");
        List<DepartmentReport> deptReports = new ArrayList<>();
        
        for (Map.Entry<String, Department> entry : system.getDepartments().entrySet()) {
            String deptName = entry.getKey();
            Department dept = entry.getValue();
            List<Doctor> doctors = system.getDoctorsForDept(deptName);
            
            DepartmentReport report = new DepartmentReport();
            report.departmentName = deptName;
            report.waitingCount = dept.waitingCount();
            report.ongoingCount = dept.ongoingCount();
            report.confinedCount = dept.getConfinedSnapshot().size();
            report.doctorsCount = doctors.size();
            report.availableDoctors = (int) doctors.stream().filter(Doctor::isAvailable).count();
            
            deptReports.add(report);
        }
        
        // Sort by total patients (descending)
        quickSortDepartmentStats(deptReports, "total");
        
        for (DepartmentReport report : deptReports) {
            sb.append(report.toString()).append("\n");
        }
        
        return sb.toString();
    }
}
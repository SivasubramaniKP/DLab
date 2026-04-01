package deadlock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Probe {
    int initiatorId;
    int senderId;
    int receiverId;
    
    Probe(int initiatorId, int senderId, int receiverId) {
        this.initiatorId = initiatorId;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }
    
    void display() {
        System.out.println("Probe: (" + initiatorId + ", " + senderId + ", " + receiverId + ")");
    }
}

class Process {
    int id;
    boolean engaged;
    boolean deadlocked;
    List<Integer> waitingFor;
    List<Process> processes;
    Set<String> probeHistory;
    
    Process(int id, List<Process> processes) {
        this.id = id;
        this.processes = processes;
        this.waitingFor = new ArrayList<>();
        this.engaged = false;
        this.deadlocked = false;
        this.probeHistory = new HashSet<>();
    }
    
    void setWaitingFor(int... processIds) {
        for (int pid : processIds) {
            waitingFor.add(pid);
        }
    }
    
    synchronized void sendProbe(Probe probe) {
        System.out.print("P" + id + " sending ");
        probe.display();
        
        Process receiver = processes.get(probe.receiverId);
        receiver.receiveProbe(probe);
    }
    
    synchronized void receiveProbe(Probe probe) {
        String probeKey = probe.initiatorId + "-" + probe.senderId + "-" + probe.receiverId;
        
        if (probeHistory.contains(probeKey)) {
            return;
        }
        probeHistory.add(probeKey);
        
        System.out.print("P" + id + " received ");
        probe.display();
        
        if (probe.receiverId == id) {
            if (probe.initiatorId == id) {
                deadlockDetected(probe);
            }
            else if (waitingFor.contains(probe.initiatorId)) {
                deadlockDetected(probe);
            }
            else {
                for (int dependent : waitingFor) {
                    Probe newProbe = new Probe(probe.initiatorId, id, dependent);
                    sendProbe(newProbe);
                }
            }
        }
        else if (probe.receiverId != id) {
            for (int dependent : waitingFor) {
                Probe newProbe = new Probe(probe.initiatorId, id, dependent);
                sendProbe(newProbe);
            }
        }
    }
    
    void deadlockDetected(Probe probe) {
        deadlocked = true;
        System.out.println("\n*** DEADLOCK DETECTED ***");
        System.out.println("Processes involved in deadlock cycle:");
        System.out.println("Initiator: P" + probe.initiatorId);
        System.out.println("Path: P" + probe.senderId + " -> P" + probe.receiverId);
        System.out.println("Process P" + id + " is deadlocked\n");
    }
    
    void requestResource(int resourceId) {
        System.out.println("P" + id + " waiting for P" + resourceId);
        waitingFor.add(resourceId);
    }
}

public class ChandyMishraHaas {
    public static void main(String[] args) {
        int numProcesses = 4;
        List<Process> processes = new ArrayList<>();
        
        for (int i = 0; i < numProcesses; i++) {
            processes.add(new Process(i, processes));
        }
        
        System.out.println("=== Chandy-Mishra-Haas Deadlock Detection Algorithm ===\n");
        
        // Create deadlock scenario: P0 -> P1 -> P2 -> P3 -> P0
        processes.get(0).setWaitingFor(1);
        processes.get(1).setWaitingFor(2);
        processes.get(2).setWaitingFor(3);
        processes.get(3).setWaitingFor(0);
        
        System.out.println("Wait-For Graph:");
        System.out.println("P0 -> P1");
        System.out.println("P1 -> P2");
        System.out.println("P2 -> P3");
        System.out.println("P3 -> P0");
        System.out.println("\nInitiating deadlock detection from P0...\n");
        
        Probe initialProbe = new Probe(0, 0, 1);
        processes.get(0).sendProbe(initialProbe);
        
        System.out.println("\n=== Detection Complete ===");
        
        // Second scenario: No deadlock
        System.out.println("\n\n=== Testing No Deadlock Scenario ===");
        List<Process> processes2 = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            processes2.add(new Process(i, processes2));
        }
        
        processes2.get(0).setWaitingFor(1);
        processes2.get(1).setWaitingFor(2);
        
        System.out.println("\nWait-For Graph:");
        System.out.println("P0 -> P1");
        System.out.println("P1 -> P2");
        System.out.println("\nInitiating deadlock detection from P0...\n");
        
        Probe initialProbe2 = new Probe(0, 0, 1);
        processes2.get(0).sendProbe(initialProbe2);
        
        System.out.println("\n=== No Deadlock Detected ===");
    }
}
package CMH;

import java.util.ArrayList;
import java.util.List;

class Probe {
    int initiator;
    int sender;
    int destination;

    Probe(int initiator, int sender, int destination) {
        this.initiator = initiator;
        this.sender = sender;
        this.destination = destination;
    }

    @Override
    public String toString() {
        return " ( " + initiator + ", " + sender + ", " + destination + ")";
    }
}

class Process {
    int id;
    List<Process>  processes;
    List<Integer> DS;

    Process(int id, List<Process> processes) {
        this.id = id;
        this.processes = processes;
        this.DS = new ArrayList<>();
    }

    synchronized void sendMessage(Process receiver, Probe p) {
        receiver.receiveMessage(p);
    }

    synchronized void receiveMessage(Probe m) {
        System.out.println("Process " + id + " has received probe " + m.toString());
        if(id == m.initiator) {
            System.out.println("DEADLOCK DETECTED");
        } else {
            for(int dep : DS) {
                Probe p = new Probe(m.initiator, id, dep);
                System.out.println("Process " + id + " is sending probe " + p.toString());
                sendMessage(processes.get(dep), p);
            }
        }
    }

    synchronized void initiateDeadlockDetection() {
        for(int dep : DS) {
            Probe p = new Probe(id, id, dep);
            System.out.println("Process " + id + " is sending probe " + p.toString());
            sendMessage(processes.get(dep), p);
        }
    }
}

public class ANDPractice {
    public static void main(String[] args) {
        List<Process> processes = new ArrayList<>();
        Process p1 = new Process(0, processes);
        Process p2 = new Process(1, processes);
        Process p3 = new Process(2, processes);
        
        processes.add(p1);
        processes.add(p2);
        processes.add(p3);


        p1.DS.add(1);
        p2.DS.add(2);
        p3.DS.add(0);

        p1.initiateDeadlockDetection();
    }
}

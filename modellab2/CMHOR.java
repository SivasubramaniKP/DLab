package modellab2;


import java.util.*;
import java.util.concurrent.*;

class Message {
    enum Type { QUERY, REPLY }
    Type type;
    int initiator;
    int sender;
    int receiver;
    
    Message(Type type, int initiator, int sender, int receiver) {
        this.type = type;
        this.initiator = initiator;
        this.sender = sender;
        this.receiver = receiver;
    }
    
    void display() {
        System.out.println(type + "(" + initiator + "," + sender + "," + receiver + ")");
    }
}

class Process implements Runnable {
    int id;
    List<Integer> dependentSet;
    List<Process> processes;
    
    // Diffusion computation state
    Map<Integer, Integer> num;      // initiator -> expected reply count
    Map<Integer, Boolean> wait;     // initiator -> waiting status
    Map<Integer, Integer> engagingSender;  // initiator -> who sent engaging query
    
    boolean blocked;
    Random random;
    
    Process(int id, List<Process> processes) {
        this.id = id;
        this.processes = processes;
        this.dependentSet = new ArrayList<>();
        this.num = new HashMap<>();
        this.wait = new HashMap<>();
        this.engagingSender = new HashMap<>();
        this.blocked = true;
        this.random = new Random();
    }
    
    void setDependentSet(int... deps) {
        for (int dep : deps) {
            dependentSet.add(dep);
        }
    }
    
    synchronized void sendMessage(Process receiver, Message msg) {
        System.out.print("P" + id + " sends ");
        msg.display();
        receiver.receiveMessage(msg);
    }
    
    synchronized void receiveMessage(Message msg) {
        System.out.print("P" + id + " receives ");
        msg.display();
        
        if (msg.type == Message.Type.QUERY) {
            handleQuery(msg);
        } else {
            handleReply(msg);
        }
    }
    
    synchronized void handleQuery(Message msg) {
        int initiator = msg.initiator;
        int sender = msg.sender;
        
        // Check if this is the engaging query for process Pi
        if (!wait.containsKey(initiator)) {
            // Engaging query
            System.out.println("P" + id + " - Engaging query for initiator P" + initiator);
            wait.put(initiator, true);
            num.put(initiator, dependentSet.size());
            engagingSender.put(initiator, sender);
            
            // Send query to all dependent processes
            for (int dep : dependentSet) {
                sendMessage(processes.get(dep), 
                    new Message(Message.Type.QUERY, initiator, id, dep));
            }
            
            // If no dependents, check for deadlock immediately
            if (dependentSet.isEmpty()) {
                num.put(initiator, 0);
                checkAndSendReply(initiator);
            }
        } 
        else if (wait.get(initiator)) {
            // Already waiting - send reply back
            System.out.println("P" + id + " - Already waiting, sending REPLY");
            sendMessage(processes.get(sender), 
                new Message(Message.Type.REPLY, initiator, id, sender));
        }
    }
    
    synchronized void handleReply(Message msg) {
        int initiator = msg.initiator;
        
        if (wait.containsKey(initiator) && wait.get(initiator)) {
            int currentNum = num.get(initiator);
            currentNum--;
            num.put(initiator, currentNum);
            System.out.println("P" + id + " - num[" + initiator + "] = " + currentNum);
            
            if (currentNum == 0) {
                checkAndSendReply(initiator);
            }
        }
    }
    
    synchronized void checkAndSendReply(int initiator) {
        if (initiator == id) {
            // Deadlock detected
            System.out.println("\n*** DEADLOCK DETECTED ***");
            System.out.println("Process P" + id + " is part of deadlock cycle\n");
        } else {
            // Send reply to engaging sender
            int sender = engagingSender.get(initiator);
            System.out.println("P" + id + " sending REPLY to engaging sender P" + sender);
            sendMessage(processes.get(sender), 
                new Message(Message.Type.REPLY, initiator, id, sender));
        }
        wait.put(initiator, false);
    }
    
    synchronized void initiateDeadlockDetection() {
        System.out.println("\n=== P" + id + " initiating deadlock detection ===");
        System.out.println("Dependent set: " + dependentSet);
        
        // Send query to all processes in dependent set
        for (int dep : dependentSet) {
            sendMessage(processes.get(dep), 
                new Message(Message.Type.QUERY, id, id, dep));
        }
        
        // Initialize state
        wait.put(id, true);
        num.put(id, dependentSet.size());
        engagingSender.put(id, -1);
        
        // If no dependents, no deadlock
        if (dependentSet.isEmpty()) {
            System.out.println("P" + id + " has no dependents - no deadlock");
            wait.put(id, false);
        }
    }
    
    public void run() {
        try {
            // Let all processes start
            Thread.sleep(1000);
            
            // Only process 0 initiates detection
            if (id == 0) {
                initiateDeadlockDetection();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class CMHOR {
    public static void main(String[] args) {
        int numProcesses = 4;
        List<Process> processes = new ArrayList<>();
        
        for (int i = 0; i < numProcesses; i++) {
            processes.add(new Process(i, processes));
        }
        
        // Create wait-for graph with deadlock cycle
        // P0 -> P1 -> P2 -> P3 -> P0 (deadlock)
        processes.get(0).setDependentSet(1);
        processes.get(1).setDependentSet(2);
        processes.get(2).setDependentSet(3);
        processes.get(3).setDependentSet(0);
        
        System.out.println("=== Chandy-Mishra-Haas Diffusion Computation ===\n");
        System.out.println("Wait-For Graph:");
        System.out.println("P0 -> P1");
        System.out.println("P1 -> P2");
        System.out.println("P2 -> P3");
        System.out.println("P3 -> P0\n");
        
        // Start all process threads
        List<Thread> threads = new ArrayList<>();
        for (Process p : processes) {
            Thread t = new Thread(p);
            threads.add(t);
            t.start();
        }
        
        // Wait for completion
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("\n=== Detection Complete ===");
    }
}

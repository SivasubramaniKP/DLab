package CMH;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    synchronized void sendMessage(Process receiver, Message m) {
        receiver.receiveMessage(m);
    }

    synchronized void receiveMessage(Message msg) {
        if(msg.type == Message.Type.QUERY) handleQuery(msg);
        else handleReply(msg);
    }

    void handleQuery(Message m) {
        int initiator = m.initiator;
        int sender = m.sender;

        if(!wait.containsKey(initiator))  {
            wait.put(initiator, true);
            num.put(initiator, dependentSet.size());
            engagingSender.put(initiator, sender);

            for(int deps : dependentSet) {
                sendMessage(processes.get(deps), new Message(Message.Type.QUERY, initiator, id, deps));
            }
        } else if(wait.get(initiator)) {
            sendMessage(processes.get(sender), new Message(Message.Type.REPLY, initiator, id, sender));
        }
    }

    void handleReply(Message m) {
        int initiator = m.initiator;
        if(wait.containsKey(initiator)) {
            int currentNum = num.get(initiator);
            currentNum -= 1;
            num.put(initiator, currentNum);
            System.out.println("P" + id + " - num[" + initiator + "] = " + currentNum);

            if(currentNum == 0) {
                checkAndReply(initiator);
            }
        }
    } 

    synchronized void checkAndReply(int initiator) {
        if(initiator == id) {
            System.out.println("DEADLOCK DETECTED");
        } else {
            int sender = engagingSender.get(initiator);
            sendMessage(processes.get(sender), new Message(Message.Type.REPLY, initiator, id, sender));
        }

        wait.put(initiator, false);
    }

    synchronized void initiateDeadlock() {
        for(int dep : dependentSet) {
            sendMessage(processes.get(dep), new Message(Message.Type.QUERY, id, id, dep));
        }

        wait.put(id, true);
        num.put(id, dependentSet.size());
        engagingSender.put(id,-1);

        if(dependentSet.size() == 0) {
            System.out.println("No  deadlock");
            wait.put(id, false);
        }
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);

            if(id == 0) initiateDeadlock();
        } catch(Exception e) { e.printStackTrace(); }
    }
}
public class Practice {
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

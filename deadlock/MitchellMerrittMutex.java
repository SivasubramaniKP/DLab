package deadlock;


import java.util.*;
import java.util.concurrent.*;

class Message {
    enum Type { QUERY, REPLY, DEADLOCK }
    Type type;
    int publicId;
    int privateId;
    int senderId;
    
    Message(Type type, int publicId, int privateId, int senderId) {
        this.type = type;
        this.publicId = publicId;
        this.privateId = privateId;
        this.senderId = senderId;
    }
    
    void display() {
        System.out.println(type + " [" + publicId + "," + privateId + "] from P" + senderId);
    }
}

class Process {
    int id;
    int publicId;
    int privateId;
    boolean active;
    boolean detecting;
    List<Integer> waitingFor;
    Map<Integer, Boolean> repliesReceived;
    List<Process> processes;
    Random random = new Random();
    
    Process(int id, List<Process> processes) {
        this.id = id;
        this.processes = processes;
        this.waitingFor = new ArrayList<>();
        this.active = true;
        this.detecting = false;
        this.repliesReceived = new HashMap<>();
    }
    
    void setWaitingFor(int... processIds) {
        for (int pid : processIds) {
            waitingFor.add(pid);
        }
    }
    
    void initiateDeadlockDetection() {
        if (waitingFor.isEmpty()) {
            System.out.println("P" + id + " not waiting for any process");
            return;
        }
        
        detecting = true;
        publicId = random.nextInt(1000);
        privateId = id;
        
        System.out.println("\nP" + id + " initiating deadlock detection");
        System.out.println("Public ID: " + publicId + ", Private ID: " + privateId);
        
        for (int dependent : waitingFor) {
            Message query = new Message(Message.Type.QUERY, publicId, privateId, id);
            sendMessage(processes.get(dependent), query);
            repliesReceived.put(dependent, false);
        }
    }
    
    synchronized void sendMessage(Process receiver, Message msg) {
        System.out.print("P" + id + " sending ");
        msg.display();
        receiver.receiveMessage(msg, this);
    }
    
    synchronized void receiveMessage(Message msg, Process sender) {
        System.out.print("P" + id + " received ");
        msg.display();
        
        if (msg.type == Message.Type.QUERY) {
            handleQuery(msg, sender);
        }
        else if (msg.type == Message.Type.REPLY) {
            handleReply(msg, sender);
        }
        else if (msg.type == Message.Type.DEADLOCK) {
            handleDeadlock(msg);
        }
    }
    
    void handleQuery(Message query, Process sender) {
        if (!active) {
            Message reply = new Message(Message.Type.REPLY, query.publicId, query.privateId, id);
            sendMessage(sender, reply);
            return;
        }
        
        if (waitingFor.isEmpty()) {
            Message reply = new Message(Message.Type.REPLY, query.publicId, query.privateId, id);
            sendMessage(sender, reply);
            return;
        }
        
        if (query.publicId < publicId) {
            Message reply = new Message(Message.Type.REPLY, query.publicId, query.privateId, id);
            sendMessage(sender, reply);
        }
        else if (query.publicId > publicId) {
            for (int dependent : waitingFor) {
                Message newQuery = new Message(Message.Type.QUERY, query.publicId, query.privateId, id);
                sendMessage(processes.get(dependent), newQuery);
            }
        }
        else {
            if (query.privateId == privateId) {
                Message deadlock = new Message(Message.Type.DEADLOCK, query.publicId, query.privateId, id);
                System.out.println("\n*** DEADLOCK DETECTED ***");
                System.out.println("Process P" + id + " involved in deadlock");
                sendMessage(sender, deadlock);
            }
            else if (query.privateId < privateId) {
                Message reply = new Message(Message.Type.REPLY, query.publicId, query.privateId, id);
                sendMessage(sender, reply);
            }
            else {
                for (int dependent : waitingFor) {
                    Message newQuery = new Message(Message.Type.QUERY, query.publicId, query.privateId, id);
                    sendMessage(processes.get(dependent), newQuery);
                }
            }
        }
    }
    
    void handleReply(Message reply, Process sender) {
        if (detecting && repliesReceived.containsKey(sender.id)) {
            repliesReceived.put(sender.id, true);
            
            boolean allReplies = true;
            for (boolean received : repliesReceived.values()) {
                if (!received) {
                    allReplies = false;
                    break;
                }
            }
            
            if (allReplies) {
                System.out.println("P" + id + " received all replies - no deadlock");
                detecting = false;
                repliesReceived.clear();
            }
        }
    }
    
    void handleDeadlock(Message deadlock) {
        System.out.println("P" + id + " deadlock confirmed");
        active = false;
    }
    
    void displayStatus() {
        System.out.println("P" + id + " - Active: " + active + ", Waiting: " + waitingFor);
    }
}

public class MitchellMerrittMutex {
    public static void main(String[] args) {
        System.out.println("=== Mitchell-Merritt Distributed Deadlock Detection Algorithm ===\n");
        
        // Scenario 1: Deadlock detection
        System.out.println("--- Scenario 1: Deadlock Detection ---");
        List<Process> processes1 = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            processes1.add(new Process(i, processes1));
        }
        
        processes1.get(0).setWaitingFor(1);
        processes1.get(1).setWaitingFor(2);
        processes1.get(2).setWaitingFor(3);
        processes1.get(3).setWaitingFor(0);
        
        System.out.println("Wait-For Graph:");
        System.out.println("P0 -> P1 -> P2 -> P3 -> P0\n");
        
        processes1.get(0).initiateDeadlockDetection();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}
        
        // Scenario 2: No deadlock
        System.out.println("\n\n--- Scenario 2: No Deadlock ---");
        List<Process> processes2 = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            processes2.add(new Process(i, processes2));
        }
        
        processes2.get(0).setWaitingFor(1);
        processes2.get(1).setWaitingFor(2);
        
        System.out.println("\nWait-For Graph:");
        System.out.println("P0 -> P1 -> P2\n");
        
        processes2.get(0).initiateDeadlockDetection();
        
        System.out.println("\n=== Detection Complete ===");
    }
}
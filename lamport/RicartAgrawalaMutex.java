package lamport;

import java.util.*;
import java.util.concurrent.*;

class Message {
    enum Type { REQUEST, REPLY }
    Type type;
    int senderId;
    int timestamp;
    
    Message(Type type, int senderId, int timestamp) {
        this.type = type;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }
}

class Process implements Runnable {
    int id;
    int clock;
    boolean requested;
    boolean inCS;
    int requestTimestamp;
    boolean[] replyReceived;
    List<Process> processes;
    Queue<Message> deferredQueue;
    Random random = new Random();
    
    Process(int id, List<Process> processes) {
        this.id = id;
        this.clock = 0;
        this.processes = processes;
        this.requested = false;
        this.inCS = false;
        this.deferredQueue = new LinkedList<>();
    }
    
    synchronized void sendMessage(Process receiver, Message msg) {
        receiver.receiveMessage(msg);
    }
    
    synchronized void receiveMessage(Message msg) {
        clock = Math.max(clock, msg.timestamp) + 1;
        
        if (msg.type == Message.Type.REQUEST) {
            if (!requested && !inCS) {
                sendMessage(processes.get(msg.senderId), 
                    new Message(Message.Type.REPLY, id, clock));
            }
            else if (inCS) {
                deferredQueue.add(msg);
            }
            else if (requested) {
                if (msg.timestamp < requestTimestamp) {
                    deferredQueue.add(msg);
                }
                else {
                    sendMessage(processes.get(msg.senderId), 
                        new Message(Message.Type.REPLY, id, clock));
                }
            }
        }
        else if (msg.type == Message.Type.REPLY) {
            if (requested && !replyReceived[msg.senderId]) {
                replyReceived[msg.senderId] = true;
            }
        }
    }
    
    synchronized void requestCS() {
        requested = true;
        clock++;
        requestTimestamp = clock;
        replyReceived = new boolean[processes.size()];
        
        Message req = new Message(Message.Type.REQUEST, id, clock);
        for (Process p : processes) {
            if (p.id != id) {
                sendMessage(p, req);
            }
        }
    }
    
    synchronized boolean canEnterCS() {
        for (int i = 0; i < processes.size(); i++) {
            if (i != id && !replyReceived[i]) {
                return false;
            }
        }
        return true;
    }
    
    synchronized void releaseCS() {
        inCS = false;
        requested = false;
        
        while (!deferredQueue.isEmpty()) {
            Message deferred = deferredQueue.poll();
            sendMessage(processes.get(deferred.senderId), 
                new Message(Message.Type.REPLY, id, clock));
        }
        notifyAll();
    }
    
    public void run() {
        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(random.nextInt(1000));
                
                synchronized (this) {
                    System.out.println("P" + id + " requesting CS at clock: " + clock);
                    requestCS();
                    
                    while (!canEnterCS()) {
                        wait();
                    }
                    
                    inCS = true;
                    System.out.println("P" + id + " ENTERED CS | Timestamp: " + requestTimestamp);
                }
                
                Thread.sleep(500);
                
                synchronized (this) {
                    System.out.println("P" + id + " EXITING CS");
                    releaseCS();
                }
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class RicartAgrawalaMutex {
    public static void main(String[] args) {
        int numProcesses = 3;
        List<Process> processes = new ArrayList<>();
        
        for (int i = 0; i < numProcesses; i++) {
            processes.add(new Process(i, processes));
        }
        
        System.out.println("=== Ricart-Agrawala Mutual Exclusion Algorithm ===\n");
        
        List<Thread> threads = new ArrayList<>();
        for (Process p : processes) {
            Thread t = new Thread(p);
            threads.add(t);
            t.start();
        }
        
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("\n=== Execution Completed ===");
    }
}
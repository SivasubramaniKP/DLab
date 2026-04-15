package modellab2;
import java.util.*;
import java.util.concurrent.*;

class Message {
    enum Type { REQUEST, REPLY, RELEASE }
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
    PriorityQueue<Message> queue;
    int replyCount;
    boolean requested;
    boolean inCS;
    List<Process> processes;
    Random random = new Random();
    
    Process(int id, List<Process> processes) {
        this.id = id;
        this.clock = 0;
        this.processes = processes;
        this.queue = new PriorityQueue<>(Comparator.comparingInt(m -> m.timestamp));
        this.replyCount = 0;
        this.requested = false;
        this.inCS = false;
    }
    
    synchronized void sendMessage(Process receiver, Message msg) {
        receiver.receiveMessage(msg);
    }
    
    synchronized void receiveMessage(Message msg) {
        clock = Math.max(clock, msg.timestamp) + 1;
        
        if (msg.type == Message.Type.REQUEST) {
            queue.add(msg);
            sendMessage(processes.get(msg.senderId), 
                new Message(Message.Type.REPLY, id, clock));
        }
        else if (msg.type == Message.Type.REPLY) {
            if (requested && !inCS) replyCount++;
        }
        else if (msg.type == Message.Type.RELEASE) {
            queue.removeIf(m -> m.senderId == msg.senderId);
        }
    }
    
    synchronized void requestCS() {
        requested = true;
        clock++;
        Message req = new Message(Message.Type.REQUEST, id, clock);
        for (Process p : processes) {
            if (p.id != id) sendMessage(p, req);
        }
        queue.add(req);
        replyCount = 0;
    }
    
    synchronized void releaseCS() {
        requested = false;
        inCS = false;
        queue.removeIf(m -> m.senderId == id);
        Message release = new Message(Message.Type.RELEASE, id, clock);
        for (Process p : processes) {
            if (p.id != id) sendMessage(p, release);
        }
    }
    
    synchronized boolean canEnterCS() {
        if (queue.isEmpty()) return false;
        Message first = queue.peek();
        return requested && !inCS && first.senderId == id && replyCount == processes.size() - 1;
    }
    
    public void run() {
        for (int i = 0; i < 2; i++) {
            try {
                Thread.sleep(random.nextInt(1000));
                System.out.println("P" + id + " requesting CS");
                requestCS();
                synchronized (this) {
                    while (!canEnterCS()) wait();
                }
                inCS = true;
                System.out.println("P" + id + " ENTERED CS");
                Thread.sleep(500);
                System.out.println("P" + id + " EXITING CS");
                releaseCS();
                synchronized (this) { notifyAll(); }
            } catch (InterruptedException e) {}
        }
    }
}

public class Lamport {
    public static void main(String[] args) {
        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < 3; i++) processes.add(new Process(i, processes));
        for (Process p : processes) new Thread(p).start();
    }
}

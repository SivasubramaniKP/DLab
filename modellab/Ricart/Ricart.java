package modellab.Ricart;

import java.util.ArrayList;
import java.util.List;

enum MessageType {
    REQUEST,
    REPLY
}

class Message {
    int processid;
    int timestamp;
    MessageType type;
    
    Message(int processid, int timestamp, MessageType type) {
        this.processid = processid;
        this.timestamp = timestamp;
        this.type = type;
    }
}

class Process implements Runnable {
    int processid;
    int clock;
    int replyCount;
    int requestTimestamp;

    boolean inCS;
    boolean requested;

    List<Process> processes;
    List<Integer> deferred;

    Process(int id, List<Process> processes) {
        this.processid = id;
        this.processes = processes;
        this.clock = 0;

        this.replyCount = 0;
        this.inCS = false;
        this.requested = false;

        this.deferred = new ArrayList<>();
    }

    void sendMessage(Process receiver, Message m) {
        receiver.receiveMessage(m);
    }

    synchronized void receiveMessage(Message m) {
        clock = Math.max(clock, m.timestamp) + 1;

        if(m.type == MessageType.REQUEST) {
            boolean myRequestHasPriority = requested && 
                    ( ourTimeStamp() < m.timestamp || ourTimeStamp() == m.timestamp && processid < m.processid );
            if(inCS || myRequestHasPriority) deferred.add(m.processid);
            else {
                Message reply = new Message(processid, clock, MessageType.REPLY);
                Process sender = processes.get(m.processid);
                new Thread(() -> {
                    sendMessage(sender, reply);
                }).start();
            }
        } else if (m.type == MessageType.REPLY) {
            if(requested && !inCS){
                replyCount += 1;
                notifyAll();
            }
        }
    }

    synchronized void requestCS() {
        requested = true;
        clock += 1;
        requestTimestamp = clock;
        int myTimestamp = clock;

        List<Process> targets = new ArrayList<>(processes);
        Message m = new Message(processid, myTimestamp, MessageType.REQUEST);
        replyCount = 0;

        new Thread(() -> {
            for(Process p : targets) {
                if(p.processid != processid) sendMessage(p, m);
            }
        }).start();
    }

    synchronized void releaseCS() {
        requested = false;
        inCS = false;

        Message m = new Message(processid, clock, MessageType.REPLY);
        List<Integer> toBeNotified = new ArrayList<>(deferred);
        deferred.clear();

        new Thread(() -> {
            for(Integer p : toBeNotified) {
                sendMessage(processes.get(p), m);
            }
        }
        ).start();;
    }

    private int ourTimeStamp() {
        return requestTimestamp;
    }

    synchronized boolean canEnterCS() {
        return requested && !inCS && replyCount == processes.size() - 1;
    }

    public void run() {
        try {
            for(int i = 0; i < 2; i++) {
                Thread.sleep(1000);
                System.out.println("Process " + processid + " has requested to Enter CS");
                requestCS();

                synchronized (this) {
                    while(!canEnterCS()) wait();
                    inCS = true;
                }

                System.out.println("Process " + processid + " has Entered the CS");
                Thread.sleep(5000);
                System.out.println("Process " + processid + " has completed the CS");
                releaseCS();
            }
        } catch(Exception e) {

        }
    }
}


public class Ricart {
    public static void main(String[] args) {
        List<Process> processes = new ArrayList<>();

        for(int i = 0; i < 3; i++) processes.add(new Process(i, processes));
        for(Process p : processes) new Thread(p).start();
    }    
}

package terminal.Ricart;

import java.lang.annotation.Repeatable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

class Message {
    enum Type { REQUEST, REPLY };
    Type type;
    int senderID;
    int timestamp;

    Message(Type type, int senderID, int timestamp) {
        this.type = type;
        this.senderID = senderID;
        this.timestamp = timestamp;
    }
}

class Process implements Runnable {

    int id;
    List<Process> processes;
    Queue<Integer> deferredQueue;

    int timestamp;
    boolean requested;
    boolean inCS;

    int numProcesses;
    int acks;

    Process(int id, List<Process> processes, int numProcesses, int timestamp)  {
        this.id = id;
        this.processes = processes;
        this.numProcesses = numProcesses;
        this.deferredQueue = new LinkedList<>();
        this.timestamp = timestamp;
        this.requested = false;
        this.inCS = false;
        this.acks = 0;
    }

    void sendMessage(Process receiver, Message m){
        receiver.receiveMessage(m);
    }

    synchronized void receiveMessage(Message m ){
        if(m.type == Message.Type.REQUEST) {
            // if(!requested && !inCS) { 
            //     System.out.println("Process " + id + " is not in CS neither requested so sending REPLY to " + m.senderID);
            //     sendMessage(processes.get(m.senderID), new Message(Message.Type.REPLY, id, timestamp)); 
            // }
            // if(requested && timestamp < m.timestamp) {
            //     System.out.println("Process " + id + " is adding " + m.senderID  + " to the deferred queue");
            //     deferredQueue.add(m.senderID);
            // }
            // ✅ Correct
                if (inCS || (requested && timestamp < m.timestamp)) {
                    deferredQueue.add(m.senderID);  // defer — I have priority or I'm in CS
                } else {
                    sendMessage(processes.get(m.senderID), new Message(Message.Type.REPLY, id, timestamp));
                }
        }
        if(requested && m.type == Message.Type.REPLY) {
            acks += 1;
            System.out.println("NO OF ACKS FOR Process " + id + " is " + acks);
            if(acks == numProcesses - 1) executeCS();
        }
    }

    synchronized void requestCS() {
        int ts = timestamp;
        requested = true;

        System.out.println("INSIDE REQUEST CS");
        for(Process p : processes) {
            if(p.id != id) {
                sendMessage(p, new Message(Message.Type.REQUEST, id, ts));
            }
        }
    }


     void executeCS() {
        try {
            inCS = true;
            System.out.println("Process " + id + " has entered the critical section ");
            Thread.sleep(1000);
            System.out.println("Process " + id + " has left the critical section ");
            releaseCS();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
    void releaseCS() {
        Queue<Integer> toNotify;
        synchronized(this) {
            requested = false;
            inCS = false;
            acks = 0;
            toNotify = new LinkedList<>(deferredQueue);
        }
        System.out.println("IN RELEASE CS");

        while(!toNotify.isEmpty()) {
            int p = toNotify.poll();
            System.out.println("Process " + id + " sending reply to the process " + p + " which was in deferred queue");
            sendMessage(processes.get(p), new Message(Message.Type.REPLY, id, timestamp));
        }
    }

    @Override
    public void run() {
        try {
            Random random = new Random();
            Thread.sleep(random.nextInt(3000));
            System.out.println("Process " + id + " wants CS Now");
            requestCS();
        } catch(Exception e){ 
            e.printStackTrace();
        }
    }

}

public class RicartAgrawala {

    public static void main(String[] args) {
        int numProcesses = 3;
        List<Process>  processes = new ArrayList<>();

        Process p1 = new Process(0, processes, numProcesses, 0);
        Process p2 = new Process(1, processes, numProcesses, 2);
        Process p3 = new Process(2, processes, numProcesses, 1);

        processes.add(p1);
        processes.add(p2);
        processes.add(p3);

        new Thread(p1).start();
        new Thread(p2).start();
        new Thread(p3).start();

    }
}

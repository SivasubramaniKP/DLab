package modellab2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class MessageMM {
    enum Type { QUERY, REPLY }
    Type type;
    int publicId;
    int privateId;
    int senderId;
    
    MessageMM(Type type, int publicId, int privateId, int senderId) {
        this.type = type;
        this.publicId = publicId;
        this.privateId = privateId;
        this.senderId = senderId;
    }
}

class ProcessMM implements Runnable {
    int id;
    int publicId;
    int privateId;
    List<Integer> waitingFor;
    List<ProcessMM> processes;
    boolean active;
    Random random = new Random();
    
    ProcessMM(int id, List<ProcessMM> processes) {
        this.id = id;
        this.processes = processes;
        this.waitingFor = new ArrayList<>();
        this.active = true;
    }
    
    void setWaitingFor(int... ids) {
        for (int pid : ids) waitingFor.add(pid);
    }
    
    synchronized void sendMessage(ProcessMM receiver, MessageMM msg) {
        System.out.println("P" + id + " -> P" + receiver.id + ": " + msg.type);
        receiver.receiveMessage(msg);
    }
    
    synchronized void receiveMessage(MessageMM msg) {
        if (msg.type == MessageMM.Type.QUERY) {
            if (!active || waitingFor.isEmpty()) {
                sendMessage(processes.get(msg.senderId), 
                    new MessageMM(MessageMM.Type.REPLY, msg.publicId, msg.privateId, id));
            }
            else if (msg.publicId > publicId) {
                for (int dep : waitingFor) {
                    sendMessage(processes.get(dep), 
                        new MessageMM(MessageMM.Type.QUERY, msg.publicId, msg.privateId, id));
                }
            }
            else if (msg.publicId == publicId && msg.privateId == privateId) {
                System.out.println("\n*** DEADLOCK DETECTED *** Process P" + id);
            }
        }
    }
    
    void initiateDetection() {
        publicId = random.nextInt(1000);
        privateId = id;
        System.out.println("\nP" + id + " initiating detection with ID: " + publicId);
        for (int dep : waitingFor) {
            sendMessage(processes.get(dep), 
                new MessageMM(MessageMM.Type.QUERY, publicId, privateId, id));
        }
    }
    
    public void run() {
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        initiateDetection();
    }
}

public class MM {
    public static void main(String[] args) {
        List<ProcessMM> processes = new ArrayList<>();
        for (int i = 0; i < 3; i++) processes.add(new ProcessMM(i, processes));
        
        processes.get(0).setWaitingFor(1);
        processes.get(1).setWaitingFor(2);
        processes.get(2).setWaitingFor(0);
        
        System.out.println("Wait-For: P0->P1->P2->P0\n");
        new Thread(processes.get(0)).start();
    }
}
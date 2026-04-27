package terminal.clocks;

import java.util.ArrayList;
import java.util.List;

class Message {
    int senderId;
    int senderTimeStamp;
    int receiver;
    String id;

    Message(String id, int senderId, int receiver) {
        this.id = id;
        this.senderId = senderId;
        this.receiver = receiver;
        senderTimeStamp = -1;
    }
}

class Process {

    int id;
    List<Process> processes;
    int numProcesses;
    int clock;
    int dx;
    List<Message> messageQueue;

    Process(int id, List<Process> processes, int numProcesses) {
        this.id = id;
        this.processes = processes;
        this.numProcesses = numProcesses;
        this.dx = 1;
        this.clock = 0;
        messageQueue = new ArrayList<>();
    }

    void internalEvent() {
        clock += dx;
        System.out.println("An internal event has occured in Process " + (id + 1) + " and now the clock is " + clock);
    }

    void sendMessage(Process receiver, Message m) {
        clock += 1;
        m.senderTimeStamp = clock;
        System.out.println("[" + m.id.charAt(0) + "] = " + clock);
        receiver.messageQueue.add(m);
    }

    void receiveMessage(String m) {
        for(Message msg : messageQueue) {
            if(msg.id.equals(m)) {
                int msgTimestamp = msg.senderTimeStamp;
                int newtime = Math.max(msgTimestamp, clock) + 1;
                System.out.println("Now the clock is [" + m.charAt(1) + "] = " + newtime);
                clock = newtime;
            }
        }
    }
}

public class LogicalClock {

    public static void main(String[] args) {
        List<Process> processes = new ArrayList<>();
        Process p1 = new Process(0, processes, 4)   ;
        Process p2 = new Process(1, processes, 4)   ;
        Process p3 = new Process(2, processes, 4)   ;
        Process p4 = new Process(3, processes, 4)   ;

        Message aj = new Message("aj", 0, 1);
        Message bl = new Message("bl", 0, 2);
        Message ch = new Message("ch", 0, 1);
        Message pd = new Message("pd", 3, 0);
        Message ie = new Message("ie",1, 0);
        Message rf = new Message("rf", 3, 0); 
        Message mk = new Message("mk", 2, 1);
        Message nq = new Message("nq", 2, 3);
        Message os = new Message("os", 2, 3);


        p1.sendMessage(p2, aj);
        p1.sendMessage(p3, bl);
        p1.sendMessage(p2, ch);

        p4.sendMessage(p1, pd);
        p1.receiveMessage("pd");

        p2.internalEvent();
        p2.receiveMessage("ch");
        p2.sendMessage(p1, ie);
        p1.receiveMessage("ie");
        p2.receiveMessage("aj");

        p3.receiveMessage("bl");
        p3.sendMessage(p2, mk);
        p3.sendMessage(p4, nq);
        p3.sendMessage(p4, os);

        p4.receiveMessage("nq");
        p4.sendMessage(p1, rf);
        p4.receiveMessage("os");
        
        p3.receiveMessage("mk");


    }

}

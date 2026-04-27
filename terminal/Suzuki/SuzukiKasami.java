package terminal.Suzuki;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;


class Message {
    enum Type { REQUEST, TOKEN };
    int senderId;
    Type type;
    int requestNum;
    Token token;

    Message(Type type, int senderId, int requestNum) {
        this.type = type;
        this.senderId = senderId;
        this.requestNum = requestNum;
    }

    Message(Token token, int senderId) {
        this.token = token;
        this.senderId = senderId;
        this.type = Message.Type.TOKEN;
    }


}

class Token {
    int LN[];
    int numProcesses;
    Queue<Integer> queue;

    Token(int numProcesses) 
    {
        this.numProcesses = numProcesses;
        LN = new int[numProcesses];
        for(int i = 0; i < numProcesses; i++) {
            LN[i] = 0;
        }
        queue = new LinkedList<>();
    }
    
    void printToken() {
        for(int i = 0; i < numProcesses; i++) {
            System.out.println("LN [" + i + " ] = " + LN[i]);
        }

        System.out.println("QUEUE : ");
        for(Integer i : queue) {
                System.out.println(i);
        }
        System.out.println("End of token\n\n\n");
        

    }

}

class Process implements Runnable {

    int id;
    List<Process> processes;
    int numProcesses;

    boolean inCS;
    boolean hasToken;
    boolean requested;
    
    int RN[];
    Token token;

    void printRN () {
        System.out.println("RN of process " + id);
        for(int i  = 0 ; i < numProcesses; i++) {
            System.out.println(" RN [ " + i + " ] = " + RN[i] );
        }
    }
    Process(int id, List<Process> processes, int numProcesses){
        this.id = id;
        this.processes = processes;
        this.numProcesses = numProcesses;
        this.inCS = false;
        this.hasToken = (id == 0);
        this.requested = false;

        this.RN = new int[numProcesses];
        this.token = null;

        if(hasToken) {
            this.token = new Token(numProcesses);
        }
    }

    void sendMessage(Process receiver, Message m) {
        receiver.receiveMessage(m);
    }

     void receiveMessage(Message m) 
    {
        if (m.type == Message.Type.REQUEST) {
            synchronized(this) {
                System.out.println("Process " + id + " has received message from " + m.senderId);
                if(RN[m.senderId] < m.requestNum) {
                    RN[m.senderId] = m.requestNum;

                    if(hasToken && !inCS && !requested)  {
                        sendTokenIfNeeded();
                    }
                }
            }
        } else if(m.type == Message.Type.TOKEN) {
            synchronized(this) {
                hasToken = true;
                token = m.token;
                System.out.println("Process " + id + " has received the token");
                token.printToken();
            }
            if(requested) {
                executeCS();
            }
        }
    }

     void executeCS() {
        try {
            inCS = true;
            System.out.println("Process " + id + " is entering the CS");
            Thread.sleep(1000);
            System.out.println("Process " + id + " is leaving the CS");
            releaseCS();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void requestCS() {
    int currentRN;
    synchronized(this) {
        if (hasToken) {
            if (!inCS) executeCS();
            return;
        }
        RN[id]++;
        currentRN = RN[id];
        requested = true;
    }
    // Sending is OUTSIDE sync so we don't hold our lock while waiting for others
    for (Process p : processes) {
        if (p.id != id) sendMessage(p, new Message(Message.Type.REQUEST, id, currentRN));
    }
}

     void releaseCS() {
        inCS = false;
        requested = false;

        token.LN[id] = RN[id];

        sendTokenIfNeeded();
    }

     void sendTokenIfNeeded() {
        synchronized(this) {
            if(token == null) return;

            for(int i = 0; i < numProcesses; i++) {
                if(!token.queue.contains(i) && RN[i] == token.LN[i] + 1) {
                    System.out.println("Process " + i  + " is added to the queue");
                    token.queue.add(i);
                }
            }

            if (!token.queue.isEmpty()){
                int nextProcess = token.queue.poll();
                System.out.println("Token is being passed to " + nextProcess);
                sendMessage(processes.get(nextProcess), new Message(token, id));
                hasToken = false;
                token = null;
            }

        }
    }

    @Override
    public void run() {
        Random random = new Random();
        try  {
            int i = 0;
            while (i < 3) {
                Thread.sleep(random.nextInt(3000));
                System.out.println("Process " + id + " now wants CS");
                requestCS();
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

public class SuzukiKasami {
    public static void main(String[] args) {
        List<Process> processes = new ArrayList<>();
        int numProcesses = 3;
        Process p1 = new Process(0, processes, numProcesses);
        Process p2 = new Process(1, processes, numProcesses);
        Process p3 = new Process(2, processes, numProcesses);

        processes.add(p1);
        processes.add(p2);
        processes.add(p3);

        List<Thread> threads = new ArrayList<>();
        for(Process p : processes) {
            threads.add(new Thread(p));
        }

        for(Thread t : threads) {
            t.start();
        }
        try {
            for(Thread t : threads) t.join();
        } catch (Exception e)  {
            e.printStackTrace();
        }

    }
}
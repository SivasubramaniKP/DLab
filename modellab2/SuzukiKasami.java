
package modellab2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

class Message {
    enum Type { REQUEST, TOKEN }
    Type type;
    int senderId;
    int sequenceNum;
    Token token;  // ✅ ADD THIS - store token object for TOKEN messages
    
    // Constructor for REQUEST messages
    Message(Type type, int senderId, int sequenceNum) {
        this.type = type;
        this.senderId = senderId;
        this.sequenceNum = sequenceNum;
        this.token = null;
    }
    
    // Constructor for TOKEN messages
    Message(Token token, int senderId) {
        this.type = Type.TOKEN;
        this.token = token;
        this.senderId = senderId;
        this.sequenceNum = 0;
    }
}

class Token {
    int[] lastRequestGranted;
    Queue<Integer> requestQueue;
    
    Token(int numProcesses) {
        this.lastRequestGranted = new int[numProcesses];
        this.requestQueue = new LinkedList<>();
        for (int i = 0; i < numProcesses; i++) {
            lastRequestGranted[i] = 0;
        }
    }
}

class Process implements Runnable {
    int id;
    int[] requestNum;
    boolean hasToken;
    boolean requested;
    boolean inCS;
    Token token;
    List<Process> processes;
    Random random = new Random();
    
    Process(int id, List<Process> processes) {
        this.id = id;
        this.processes = processes;
        this.requestNum = new int[processes.size()];
        this.hasToken = (id == 0);
        this.requested = false;
        this.inCS = false;
        
        if (hasToken) {
            this.token = new Token(processes.size());
        }
    }
    
    synchronized void sendMessage(Process receiver, Message msg) {
        System.out.println("P" + id + " sends " + msg.type + " to P" + receiver.id);
        receiver.receiveMessage(msg);
    }
    
    synchronized void receiveMessage(Message msg) {
        if (msg.type == Message.Type.REQUEST) {
            System.out.println("P" + id + " received REQUEST from P" + msg.senderId + " (seq:" + msg.sequenceNum + ")");
            
            requestNum[msg.senderId] = Math.max(requestNum[msg.senderId], msg.sequenceNum);
            
            if (hasToken && !inCS && !requested) {
                sendToken();
            }
        }
        else if (msg.type == Message.Type.TOKEN) {
            System.out.println("P" + id + " received TOKEN");
            this.token = msg.token;  // ✅ FIXED - get token from message
            this.hasToken = true;
            
            if (requested) {
                executeCS();
            }
            else if (!token.requestQueue.isEmpty()) {
                sendToken();
            }
        }
    }
    
    synchronized void requestCS() {
        requested = true;
        requestNum[id]++;
        
        System.out.println("\nP" + id + " REQUESTING CS (RN[" + id + "] = " + requestNum[id] + ")");
        
        if (hasToken) {
            executeCS();
        } else {
            Message req = new Message(Message.Type.REQUEST, id, requestNum[id]);
            for (Process p : processes) {
                if (p.id != id) {
                    sendMessage(p, req);
                }
            }
        }
    }
    
    synchronized void executeCS() {
        try {
            inCS = true;
            System.out.println("\n***** P" + id + " ENTERED CRITICAL SECTION *****\n");
            Thread.sleep(1000);
            System.out.println("P" + id + " EXITING CRITICAL SECTION\n");
            releaseCS();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    synchronized void releaseCS() {
        inCS = false;
        requested = false;
        
        // Update token's last request granted
        token.lastRequestGranted[id] = requestNum[id];
        
        // Check for pending requests
        for (int i = 0; i < processes.size(); i++) {
            if (i != id && requestNum[i] > token.lastRequestGranted[i]) {
                if (!token.requestQueue.contains(i)) {
                    token.requestQueue.add(i);
                }
            }
        }
        
        if (!token.requestQueue.isEmpty()) {
            sendToken();
        }
    }
    
    synchronized void sendToken() {
        int next = token.requestQueue.poll();
        System.out.println("P" + id + " sending TOKEN to P" + next);
        hasToken = false;
        // ✅ FIXED - use TOKEN constructor
        sendMessage(processes.get(next), new Message(token, id));
    }
    
    public void run() {
        for (int i = 0; i < 2; i++) {
            try {
                Thread.sleep(random.nextInt(1500) + 500);
                requestCS();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class SuzukiKasami {
    public static void main(String[] args) {
        int numProcesses = 3;
        List<Process> processes = new ArrayList<>();
        
        for (int i = 0; i < numProcesses; i++) {
            processes.add(new Process(i, processes));
        }
        
        System.out.println("=== Suzuki-Kasami Token-Based Mutual Exclusion ===\n");
        System.out.println("Initial token with P0\n");
        
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
        
        System.out.println("\n=== Execution Complete ===");
    }
}
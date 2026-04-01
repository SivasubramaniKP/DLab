package lamport;
import java.util.*;
import java.util.concurrent.*;

class Message {
    enum Type { REQUEST, TOKEN }
    Type type;
    int senderId;
    int sequenceNum;
    Token token;

    Message(Type type, int senderId, int sequenceNum) {
        this.type = type;
        this.senderId = senderId;
        this.sequenceNum = sequenceNum;
        this.token = null;
    }

    Message(Token token, int senderId) {
        this.type = Type.TOKEN;
        this.token = token;
        this.senderId = senderId;
        this.sequenceNum = 0;
    }
}

class Token {
    int[] sequenceNum;
    Queue<Integer> queue;

    Token(int numProcesses) {
        this.sequenceNum = new int[numProcesses];
        this.queue = new LinkedList<>();
    }
}

class Process implements Runnable {
    int id;
    int numProcesses;
    boolean hasToken;
    boolean requested;
    boolean inCS;
    int[] sequenceNum;
    Queue<Integer> tokenQueue;
    List<Process> processes;
    Token token;
    Random random = new Random();

    Process(int id, int numProcesses, List<Process> processes) {
        this.id = id;
        this.numProcesses = numProcesses;
        this.processes = processes;
        this.hasToken = (id == 0);
        this.requested = false;
        this.inCS = false;
        this.sequenceNum = new int[numProcesses];
        this.tokenQueue = new LinkedList<>();

        if (hasToken) {
            this.token = new Token(numProcesses);
        }
    }

    // FIX 1: NOT synchronized — avoids circular lock deadlock
    void sendMessage(Process receiver, Message msg) {
        receiver.receiveMessage(msg);
    }

    synchronized void receiveMessage(Message msg) {
        if (msg.type == Message.Type.REQUEST) {
            sequenceNum[msg.senderId] = Math.max(sequenceNum[msg.senderId], msg.sequenceNum);

            // FIX 4: Avoid duplicate entries in the queue
            if (!tokenQueue.contains(msg.senderId)) {
                tokenQueue.add(msg.senderId);
            }

            if (hasToken && !inCS && !requested) {
                sendToken();
            }
        } else if (msg.type == Message.Type.TOKEN) {
            this.token = msg.token;
            hasToken = true;

            // FIX 3: Copy arrays — don't alias the token's internals
            this.sequenceNum = Arrays.copyOf(this.token.sequenceNum, numProcesses);
            this.tokenQueue = new LinkedList<>(this.token.queue);

            if (requested) {
                enterCS();
            } else if (!tokenQueue.isEmpty()) {
                sendToken();
            }
        }
    }

    synchronized void requestCS() {
        requested = true;
        sequenceNum[id]++;

        if (hasToken) {
            enterCS();
        } else {
            System.out.println("P" + id + " requesting CS | Seq: " + sequenceNum[id]);
            Message req = new Message(Message.Type.REQUEST, id, sequenceNum[id]);
            for (Process p : processes) {
                if (p.id != id) {
                    sendMessage(p, req);
                }
            }
        }
    }

    // FIX 5: Only mark entry inside synchronized; sleep happens outside in run()
    synchronized void enterCS() {
        inCS = true;
        System.out.println("P" + id + " ENTERED CS | Seq: " + sequenceNum[id]);
    }

    synchronized void exitCS() {
        System.out.println("P" + id + " EXITING CS");
        inCS = false;
        requested = false;

        token.sequenceNum = Arrays.copyOf(sequenceNum, numProcesses);
        token.queue = new LinkedList<>(tokenQueue);

        if (!tokenQueue.isEmpty()) {
            sendToken();
        }
    }

    synchronized void sendToken() {
        int next = tokenQueue.poll();
        hasToken = false;

        // FIX 2: Deep-copy token before sending — don't share the same object
        Token copy = new Token(numProcesses);
        copy.sequenceNum = Arrays.copyOf(sequenceNum, numProcesses);
        copy.queue = new LinkedList<>(tokenQueue);

        System.out.println("P" + id + " sending token to P" + next);
        Message tokenMsg = new Message(copy, id);
        sendMessage(processes.get(next), tokenMsg);
    }

    public void run() {
        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(random.nextInt(1000));
                requestCS();

                // Wait until actually in CS, then simulate work outside the lock
                while (true) {
                    synchronized (this) { if (inCS) break; }
                    Thread.sleep(10);
                }

                Thread.sleep(500); // Simulate work — no lock held here

                exitCS();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class SuzukiKasamiMutex {
    public static void main(String[] args) {
        int numProcesses = 3;
        List<Process> processes = new ArrayList<>();

        for (int i = 0; i < numProcesses; i++) {
            processes.add(new Process(i, numProcesses, processes));
        }

        System.out.println("=== Suzuki-Kasami Mutual Exclusion Algorithm ===\n");
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

        System.out.println("\n=== Execution Completed ===");
    }
}
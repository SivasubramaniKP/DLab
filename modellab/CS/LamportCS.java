

package modellab.CS;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

enum Type { REQUEST, REPLY, RELEASE }

class Message {
    Type type;
    int senderid;
    int timestamp;

    Message(Type type, int senderid, int timestamp) {
        this.type = type;
        this.senderid = senderid;
        this.timestamp = timestamp;
    }
}

class Process implements Runnable {
    int processid;
    int clock;
    int replyCount;
    boolean requested;
    boolean inCS;
    PriorityQueue<Message> queue;
    List<Process> processes;

    Process(int id, List<Process> processes) {
        this.processid = id;
        this.clock = 0;
        this.replyCount = 0;
        this.requested = false;
        this.inCS = false;
        this.queue = new PriorityQueue<>(Comparator.comparingInt((Message m) -> m.timestamp)
                                        .thenComparingInt(m -> m.senderid)); // tie-break by ID
        this.processes = processes;
    }

    // ✅ NOT synchronized — just a forwarding helper, no shared state accessed
    void sendMessage(Process p, Message m) {
        p.receiveMessage(m);
    }

    synchronized void receiveMessage(Message m) {
        clock = Math.max(clock, m.timestamp) + 1;

        if (m.type == Type.REQUEST) {
            queue.add(m);
            // ✅ Build reply first, then send AFTER releasing lock
            Message reply = new Message(Type.REPLY, processid, clock);
            Process sender = processes.get(m.senderid);
            // Release lock before sending to avoid nested lock deadlock
            notifyAll();
            // Send outside synchronized — done via a local variable captured before
            new Thread(() -> sendMessage(sender, reply)).start();

        } else if (m.type == Type.REPLY) {
            if (requested && !inCS) {
                replyCount++;
                notifyAll();
            }
        } else if (m.type == Type.RELEASE) {
            queue.removeIf(msg -> msg.senderid == m.senderid);
            notifyAll();
        }
    }

    synchronized void requestCS() {
        requested = true;
        clock++;
        Message m = new Message(Type.REQUEST, processid, clock);
        queue.add(m);
        replyCount = 0;

        // ✅ Capture targets before releasing lock
        List<Process> targets = new ArrayList<>(processes);
        // Send OUTSIDE lock to avoid deadlock
        new Thread(() -> {
            for (Process p : targets) {
                if (p.processid != processid) {
                    sendMessage(p, m);
                }
            }
        }).start();
    }

    synchronized void releaseCS() {
        inCS = false;
        requested = false;
        queue.removeIf(msg -> msg.senderid == processid);

        Message m = new Message(Type.RELEASE, processid, clock);
        List<Process> targets = new ArrayList<>(processes);

        // ✅ Send OUTSIDE lock
        new Thread(() -> {
            for (Process p : targets) {
                if (p.processid != processid) {
                    sendMessage(p, m);
                }
            }
        }).start();
    }

    synchronized boolean canEnterCS() {
        if (queue.isEmpty()) return false;
        Message first = queue.peek();
        return requested && !inCS
                && first.senderid == processid
                && replyCount == processes.size() - 1;
    }

    @Override
    public void run() {
        for (int i = 0; i < 2; i++) {
            try {
                Thread.sleep(1000);
                System.out.println("Process " + processid + " is requesting CS");
                requestCS();

                synchronized (this) {
                    while (!canEnterCS()) wait();
                    inCS = true; // set atomically inside lock
                }

                System.out.println("Process " + processid + " has entered CS");
                Thread.sleep(500);
                System.out.println("Process " + processid + " is releasing CS");
                releaseCS();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class LamportCS {
    public static void main(String[] args) {
        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < 3; i++) processes.add(new Process(i, processes));
        for (Process p : processes) new Thread(p).start();
    }
}
import java.io.*;
import java.util.*;

class Process {
    int pid, clock;
    
    Process(int pid) {
        this.pid = pid;
        this.clock = 0;
    }
    
    void internalEvent() {
        clock++;
        System.out.println("P" + pid + " INTERNAL | Clock: " + clock);
    }
    
    int sendEvent() {
        clock++;
        System.out.println("P" + pid + " SEND    | Clock: " + clock);
        return clock;
    }
    
    void receiveEvent(int senderClock) {
        clock = Math.max(clock, senderClock) + 1;
        System.out.println("P" + pid + " RECV    | Clock: " + clock);
    }
    
    void display() {
        System.out.println("P" + pid + " Final Clock: " + clock);
    }
}

public class LogicalScalarClock {
    static Process[] processes;
    
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("1. Read from file");
        System.out.println("2. Interactive mode");
        System.out.print("Choose: ");
        int choice = sc.nextInt();
        
        if (choice == 1) {
            readFromFile();
        } else {
            interactiveMode();
        }
    }
    
    static void readFromFile() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("events.txt"));
        int n = Integer.parseInt(br.readLine());
        processes = new Process[n + 1];
        for (int i = 1; i <= n; i++) {
            processes[i] = new Process(i);
        }
        
        System.out.println("\n=== Logical Scalar Clock ===\n");
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(" ");
            String event = parts[0];
            int pid = Integer.parseInt(parts[1]);
            
            if (event.equals("INTERNAL")) {
                processes[pid].internalEvent();
            } else if (event.equals("SEND")) {
                int sender = pid;
                int receiver = Integer.parseInt(parts[2]);
                int ts = processes[sender].sendEvent();
                processes[receiver].receiveEvent(ts);
            }
        }
        br.close();
        displayFinalClocks();
    }
    
    static void interactiveMode() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();
        processes = new Process[n + 1];
        for (int i = 1; i <= n; i++) {
            processes[i] = new Process(i);
        }
        
        System.out.println("\n=== Logical Scalar Clock ===\n");
        while (true) {
            System.out.println("\n1. Internal Event");
            System.out.println("2. Send Event");
            System.out.println("3. Exit");
            System.out.print("Choose: ");
            int choice = sc.nextInt();
            
            if (choice == 1) {
                System.out.print("Enter process ID: ");
                int pid = sc.nextInt();
                processes[pid].internalEvent();
            } else if (choice == 2) {
                System.out.print("Enter sender process ID: ");
                int sender = sc.nextInt();
                System.out.print("Enter receiver process ID: ");
                int receiver = sc.nextInt();
                int ts = processes[sender].sendEvent();
                processes[receiver].receiveEvent(ts);
            } else if (choice == 3) {
                displayFinalClocks();
                break;
            }
        }
        sc.close();
    }
    
    static void displayFinalClocks() {
        System.out.println("\n=== Final Clocks ===");
        for (int i = 1; i < processes.length; i++) {
            processes[i].display();
        }
    }
}
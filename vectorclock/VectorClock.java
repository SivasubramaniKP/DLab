package vectorclock;
import java.io.*;
import java.util.*;

class Process {
    int pid;
    int[] vector;
    int numProcesses;
    
    Process(int pid, int numProcesses) {
        this.pid = pid;
        this.numProcesses = numProcesses;
        this.vector = new int[numProcesses + 1];
        for (int i = 1; i <= numProcesses; i++) {
            vector[i] = 0;
        }
    }
    
    void internalEvent() {
        vector[pid]++;
        System.out.print("P" + pid + " INTERNAL | Vector: ");
        printVector();
    }
    
    int[] sendEvent() {
        vector[pid]++;
        System.out.print("P" + pid + " SEND    | Vector: ");
        printVector();
        return vector.clone();
    }
    
    void receiveEvent(int[] senderVector) {
        for (int i = 1; i <= numProcesses; i++) {
            vector[i] = Math.max(vector[i], senderVector[i]);
        }
        vector[pid]++;
        System.out.print("P" + pid + " RECV    | Vector: ");
        printVector();
    }
    
    void printVector() {
        System.out.print("[");
        for (int i = 1; i <= numProcesses; i++) {
            System.out.print(vector[i]);
            if (i < numProcesses) System.out.print(",");
        }
        System.out.println("]");
    }
    
    void display() {
        System.out.print("P" + pid + " Final Vector: ");
        printVector();
    }
}

public class VectorClock {
    static Process[] processes;
    static int numProcesses;
    
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
        numProcesses = Integer.parseInt(br.readLine());
        processes = new Process[numProcesses + 1];
        for (int i = 1; i <= numProcesses; i++) {
            processes[i] = new Process(i, numProcesses);
        }
        
        System.out.println("\n=== Vector Clock ===\n");
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
                int[] ts = processes[sender].sendEvent();
                processes[receiver].receiveEvent(ts);
            }
        }
        br.close();
        displayFinalClocks();
    }
    
    static void interactiveMode() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of processes: ");
        numProcesses = sc.nextInt();
        processes = new Process[numProcesses + 1];
        for (int i = 1; i <= numProcesses; i++) {
            processes[i] = new Process(i, numProcesses);
        }
        
        System.out.println("\n=== Vector Clock ===\n");
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
                int[] ts = processes[sender].sendEvent();
                processes[receiver].receiveEvent(ts);
            } else if (choice == 3) {
                displayFinalClocks();
                break;
            }
        }
        sc.close();
    }
    
    static void displayFinalClocks() {
        System.out.println("\n=== Final Vectors ===");
        for (int i = 1; i <= numProcesses; i++) {
            processes[i].display();
        }
    }
}

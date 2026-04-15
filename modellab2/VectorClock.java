package modellab2;

class ProcessVC {
    int id;
    int[] vector;
    int numProcesses;
    
    ProcessVC(int id, int numProcesses) {
        this.id = id;
        this.numProcesses = numProcesses;
        this.vector = new int[numProcesses + 1];
    }
    
    void internalEvent() {
        vector[id]++;
        printVector("INTERNAL");
    }
    
    int[] sendEvent() {
        vector[id]++;
        printVector("SEND");
        return vector.clone();
    }
    
    void receiveEvent(int[] senderVector) {
        for (int i = 1; i <= numProcesses; i++) {
            vector[i] = Math.max(vector[i], senderVector[i]);
        }
        vector[id]++;
        printVector("RECV");
    }
    
    void printVector(String event) {
        System.out.print("P" + id + " " + event + " | [");
        for (int i = 1; i <= numProcesses; i++) {
            System.out.print(vector[i]);
            if (i < numProcesses) System.out.print(",");
        }
        System.out.println("]");
    }
}

public class VectorClock {
    public static void main(String[] args) {
        ProcessVC p1 = new ProcessVC(1, 3);
        ProcessVC p2 = new ProcessVC(2, 3);
        ProcessVC p3 = new ProcessVC(3, 3);
        
        p1.internalEvent();
        int[] ts = p1.sendEvent();
        p2.receiveEvent(ts);
        p2.internalEvent();
        ts = p2.sendEvent();
        p3.receiveEvent(ts);
    }
}

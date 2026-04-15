package modellab2;

class ProcessLC {
    int id;
    int clock;
    
    ProcessLC(int id) {
        this.id = id;
        this.clock = 0;
    }
    
    void internalEvent() {
        clock++;
        System.out.println("P" + id + " INTERNAL | Clock: " + clock);
    }
    
    int sendEvent() {
        clock++;
        System.out.println("P" + id + " SEND    | Clock: " + clock);
        return clock;
    }
    
    void receiveEvent(int senderClock) {
        clock = Math.max(clock, senderClock) + 1;
        System.out.println("P" + id + " RECV    | Clock: " + clock);
    }
}

public class LogicalClock {
    public static void main(String[] args) {
        ProcessLC p1 = new ProcessLC(1);
        ProcessLC p2 = new ProcessLC(2);
        ProcessLC p3 = new ProcessLC(3);
        
        p1.internalEvent();
        int ts = p1.sendEvent();
        p2.receiveEvent(ts);
        p2.internalEvent();
        ts = p2.sendEvent();
        p3.receiveEvent(ts);
        ts = p3.sendEvent();
        p1.receiveEvent(ts);
    }
}

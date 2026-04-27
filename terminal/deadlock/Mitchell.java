package terminal.deadlock;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

class Node {
    int publicLabel;
    int privateLabel;
    Node waitingFor;
    Random random;
    HashSet<Integer> acquiredLabels;

    Node(int privateLabel, HashSet<Integer> acquiredLabels) {
        random = new Random();
        this.acquiredLabels = acquiredLabels;
        while (true) {
            int plabel = random.nextInt(100);
            if (!acquiredLabels.contains(plabel)) {
                this.publicLabel = plabel;
                acquiredLabels.add(plabel);
                break; 
            }
        }
        this.privateLabel = privateLabel;
    }

    void addDependency(Node waitingFor) {
        this.waitingFor = waitingFor;
        while(true) {
            int newLabel = Math.max(waitingFor.publicLabel, publicLabel) + random.nextInt(20);
            if (!acquiredLabels.contains(newLabel)) {
                this.publicLabel = newLabel;
                this.privateLabel = newLabel;
                acquiredLabels.add(newLabel);
                break; 
            }
        }
    }

    void transmit() {
        if(waitingFor != null  && waitingFor.publicLabel > publicLabel) {
            publicLabel = waitingFor.publicLabel;
        }
    }

    boolean detect() {
        if(waitingFor == null) return false;
        if (publicLabel == privateLabel && publicLabel == waitingFor.publicLabel) {
            return true;
        } else return false;
    }
    public String toString() {
        return "";
    }
}

public class Mitchell {
    public static void main(String[] args) {
        HashSet<Integer> hashSet = new HashSet<>();
        hashSet.add(1);
        hashSet.add(2);
        hashSet.add(3);
        hashSet.add(4);

        Node node1 = new Node(1, hashSet);
        Node node2 = new Node(2, hashSet);
        Node node3 = new Node(3, hashSet);
        Node node4 = new Node(4, hashSet);

        node1.addDependency(node2);
        node2.addDependency(node3);
        node3.addDependency(node4);
        node4.addDependency(node1);

        Node [] list = {node1, node2, node3, node4};

        for(int i = 0; i < 100; i++) {
            for(int j = 0; j < 4; j++) {
                list[j].transmit();
            }
        }

        for(int i = 0; i < 4; i++) {
            System.out.println(list[i].detect());
        }

    }
}

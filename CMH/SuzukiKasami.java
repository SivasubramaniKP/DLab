package CMH;

class Token {
 int LN[];
 Token() {
    this.LN = new int[4];
 }
}

class Process implements Runnable{
    int id;
    boolean requested;
    boolean inCS;
    Token token;
    int RN[];

    Process(int id) {
        this.id = id;
        requested = false;
        inCS = false;
        token = null;
        RN = new int[4];
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }
}
public class SuzukiKasami {
    
}

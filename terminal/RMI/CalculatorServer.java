package terminal.RMI;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class CalculatorServer extends UnicastRemoteObject implements Calculator {

    public CalculatorServer() throws RemoteException {
        super();
    }

    @Override
    public int add(int a, int b) throws RemoteException {
        return a + b;

    }

    @Override
    public int sub(int a, int b) throws RemoteException {
        return a - b;

    }

    @Override
    public int mult(int a, int b) throws RemoteException {
        return a * b;
    }

    @Override
    public int divide(int a, int b) throws RemoteException {
        return a / b;
    }

    public static void main(String[] args) throws Exception {
        CalculatorServer server = new CalculatorServer(); 

        Registry registry = LocateRegistry.createRegistry(1099);

        registry.rebind("CalculatorService", server);

        System.out.println("Server is running on port 1099");
    }
    
}



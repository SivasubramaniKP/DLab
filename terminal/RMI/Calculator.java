package terminal.RMI;

import java.rmi.*;
public interface Calculator extends Remote {
    int add(int a, int b) throws RemoteException;
    int sub(int a, int b) throws RemoteException;
    int mult(int a, int b) throws RemoteException;
    int divide(int a, int b) throws RemoteException;
}

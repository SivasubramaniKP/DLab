package terminal.RMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CalculatorClient {
    public static void main(String[] args) throws Exception {
        
        Registry registry = LocateRegistry.getRegistry(1099);

        Calculator calc = (Calculator)registry.lookup("CalculatorService");

        System.out.println("6 + 7 " + calc.add(6, 7));
    } 
}

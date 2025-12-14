package phase2;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BankServer {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);

            TransactionServiceImpl transactionService = new TransactionServiceImpl();
            BankServiceImpl bankService = new BankServiceImpl(transactionService);
            transactionService.setBankService(bankService);

            registry.rebind("BankService", bankService);
            registry.rebind("TransactionService", transactionService);

            System.out.println("========================================");
            System.out.println("Bank RMI Server is running on port 1099");
            System.out.println("========================================");
            System.out.println("Available services:");
            System.out.println("  - BankService");
            System.out.println("  - TransactionService");
            System.out.println("========================================\n");

        } catch (Exception e) {
            System.err.println("Bank Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BankService extends Remote {
    // Authentication
    boolean login(String username, String password) throws RemoteException;
    String createAccount(String username, String password, double initialBalance) throws RemoteException;
    
    // Banking operations
    double checkBalance(String username) throws RemoteException;
    String deposit(String username, double amount) throws RemoteException;
    String withdraw(String username, double amount) throws RemoteException;
    String transfer(String fromUsername, String toUsername, double amount) throws RemoteException;
}

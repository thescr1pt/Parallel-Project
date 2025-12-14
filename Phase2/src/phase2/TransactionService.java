package phase2;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TransactionService extends Remote {
    // Transaction history
    List<String> getTransactionHistory(String username) throws RemoteException;
    
    // Account updates - for broadcasting to clients
    void registerClient(String username, ClientCallback callback) throws RemoteException;
    void unregisterClient(String username) throws RemoteException;
}

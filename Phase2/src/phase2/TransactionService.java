package phase2;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TransactionService extends Remote {
    List<String> getTransactionHistory(String username) throws RemoteException;

    void registerClient(String username, ClientCallback callback) throws RemoteException;
    void unregisterClient(String username) throws RemoteException;
}

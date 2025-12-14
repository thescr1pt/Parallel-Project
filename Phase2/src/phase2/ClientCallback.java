package phase2;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {
    // Called by server to notify client of updates
    void notifyUpdate(String message) throws RemoteException;
}

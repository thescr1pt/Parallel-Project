package phase2;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {
    void notifyUpdate(String message) throws RemoteException;
}

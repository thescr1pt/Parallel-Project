package phase2;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientCallbackImpl extends UnicastRemoteObject implements ClientCallback {

    public ClientCallbackImpl() throws RemoteException {
        super();
    }

    @Override
    public void notifyUpdate(String message) throws RemoteException {
        System.out.println("\n[Server Update] " + message);
        System.out.print("Choose an option: ");
    }
}

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionServiceImpl extends UnicastRemoteObject implements TransactionService {
    private BankServiceImpl bankService;
    private Map<String, ClientCallback> registeredClients;

    public TransactionServiceImpl() throws RemoteException {
        this.registeredClients = new HashMap<>();
    }

    public void setBankService(BankServiceImpl bankService) {
        this.bankService = bankService;
    }

    @Override
    public List<String> getTransactionHistory(String username) throws RemoteException {
        if (bankService == null) {
            return new ArrayList<>();
        }
        
        Account account = bankService.getAccount(username);
        if (account != null) {
            return account.getTransactionHistory();
        }
        return new ArrayList<>();
    }

    @Override
    public void registerClient(String username, ClientCallback callback) throws RemoteException {
        registeredClients.put(username.toLowerCase(), callback);
        System.out.println("Client " + username + " registered for updates.");
    }

    @Override
    public void unregisterClient(String username) throws RemoteException {
        registeredClients.remove(username.toLowerCase());
        System.out.println("Client " + username + " unregistered from updates.");
    }

    public void broadcastUpdate(String message, String excludeUsername) {
        for (Map.Entry<String, ClientCallback> entry : registeredClients.entrySet()) {
            String clientUsername = entry.getKey();
            ClientCallback callback = entry.getValue();
            
            // Don't send to the user who made the action
            if (!clientUsername.equalsIgnoreCase(excludeUsername)) {
                try {
                    callback.notifyUpdate(message);
                } catch (RemoteException e) {
                    System.err.println("Failed to notify client " + clientUsername + ": " + e.getMessage());
                    // Remove disconnected client
                    registeredClients.remove(clientUsername);
                }
            }
        }
    }
}
